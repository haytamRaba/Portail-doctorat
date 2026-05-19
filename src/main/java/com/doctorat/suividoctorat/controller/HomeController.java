package com.doctorat.suividoctorat.controller;

import com.doctorat.suividoctorat.entity.PhDRegistration;
import com.doctorat.suividoctorat.entity.User;
import com.doctorat.suividoctorat.service.*;
import jakarta.servlet.http.HttpServletRequest;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import com.doctorat.suividoctorat.dto.ProgressDTO;
import com.doctorat.suividoctorat.entity.Publication;
import com.doctorat.suividoctorat.entity.Training;
import com.doctorat.suividoctorat.entity.Campaign;


import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController {

    @Autowired
    private UserService userService;
    @Autowired
    private CampaignService campaignService;
    @Autowired
    private PhDRegistrationService phdRegistrationService;
    @Autowired
    private PrerequisiteService prerequisiteService;
    @Autowired
    private EmailService emailService;
    @Autowired
    private PDFService pdfService;
    @Autowired
    private OTPService otpService;

    private Map<String, UserRegistrationData> tempRegistrationStorage = new HashMap<>();

    private static class UserRegistrationData {
        String email;
        String password;
        String fullName;
        String role;

        UserRegistrationData(String email, String password, String fullName, String role) {
            this.email = email;
            this.password = password;
            this.fullName = fullName;
            this.role = role;
        }
    }

    @PostMapping("/register/send-otp")
    @ResponseBody
    public Map<String, String> sendOTP(@RequestParam String email,
                                       @RequestParam String password,
                                       @RequestParam String fullName,
                                       @RequestParam String role) {

        Map<String, String> response = new HashMap<>();

        if (userService.emailExists(email)) {
            response.put("status", "error");
            response.put("message", "Email already registered");
            return response;
        }

        String otp = otpService.generateOTP(email);
        otpService.sendOTPEmail(email, otp);

        tempRegistrationStorage.put(email, new UserRegistrationData(email, password, fullName, role));

        response.put("status", "success");
        response.put("message", "OTP sent to your email");
        return response;
    }

    @PostMapping("/register/verify-otp")
    @ResponseBody
    public Map<String, String> verifyOTP(@RequestParam String email,
                                         @RequestParam String otp) {

        Map<String, String> response = new HashMap<>();

        if (otpService.verifyOTP(email, otp)) {
            UserRegistrationData data = tempRegistrationStorage.get(email);

            if (data != null) {
                String result = userService.registerUser(data.email, data.password, data.fullName, data.role);

                if (result.equals("Registration successful!")) {
                    tempRegistrationStorage.remove(email);
                    response.put("status", "success");
                    response.put("message", "Registration successful! Please login.");
                } else {
                    response.put("status", "error");
                    response.put("message", result);
                }
            } else {
                response.put("status", "error");
                response.put("message", "Registration data expired. Please try again.");
            }
        } else {
            response.put("status", "error");
            response.put("message", "Invalid or expired OTP");
        }

        return response;
    }



    @GetMapping("/registration/{id}/certificate")
    public ResponseEntity<byte[]> downloadCertificate(@PathVariable Long id, HttpSession session) {
        User currentUser = getCurrentUser(session);

        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        PhDRegistration registration = phdRegistrationService.getRegistrationById(id);

        if (registration == null) {
            return ResponseEntity.notFound().build();
        }

        boolean isAuthorized = currentUser.getRole().equals("ADMIN") ||
                (currentUser.getRole().equals("DOCTORANT") && registration.getDoctorant().getId().equals(currentUser.getId()));

        if (!isAuthorized) {
            return ResponseEntity.status(403).build();
        }

        if (!registration.getStatus().equals("APPROVED")) {
            return ResponseEntity.badRequest().build();
        }

        byte[] pdfBytes = pdfService.generateRegistrationCertificate(registration);

        if (pdfBytes == null) {
            return ResponseEntity.internalServerError().build();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "certificate_" + id + ".pdf");

        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }

    @GetMapping("/registration/{id}/defense")
    public ResponseEntity<byte[]> downloadDefenseAuthorization(@PathVariable Long id, HttpSession session) {
        User currentUser = getCurrentUser(session);

        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        PhDRegistration registration = phdRegistrationService.getRegistrationById(id);

        if (registration == null) {
            return ResponseEntity.notFound().build();
        }

        boolean isAuthorized = currentUser.getRole().equals("ADMIN") ||
                (currentUser.getRole().equals("DOCTORANT") && registration.getDoctorant().getId().equals(currentUser.getId()));

        if (!isAuthorized) {
            return ResponseEntity.status(403).build();
        }

        if (!registration.getStatus().equals("APPROVED")) {
            return ResponseEntity.badRequest().build();
        }

        byte[] pdfBytes = pdfService.generateDefenseAuthorization(registration);

        if (pdfBytes == null) {
            return ResponseEntity.internalServerError().build();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "defense_auth_" + id + ".pdf");

        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }


    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("message", " PhD Tracking Portal");
        model.addAttribute("currentYear", 2026);
        return "home";
    }

    @GetMapping("/register")
    public String showRegistrationForm() {
        return "register";  // Show register.html
    }

    @PostMapping("/register")
    public String processRegistration(
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String fullName,
            @RequestParam String role,
            Model model) {

        System.out.println("=== PROCESSING REGISTRATION ===");
        System.out.println("Email: " + email);
        System.out.println("Role: " + role);

        String result = userService.registerUser(email, password, fullName, role);

        if (result.equals("Registration successful!")) {
            model.addAttribute("success", "Account created! Please login.");
            return "login";
        } else {
            model.addAttribute("error", result);
            return "register";
        }
    }
    @GetMapping("/check-session")
    @ResponseBody
    public String checkSession(HttpSession session) {
        Long userId = (Long) session.getAttribute("loggedUserId");
        User user = null;
        if (userId != null) {
            user = userService.findUserById(userId);
        }

        return "Session ID: " + session.getId() +
                "\nIs session new " + session.isNew() +
                "\nUser ID in session: " + userId +
                "\nUser from ID: " + (user != null ? user.getEmail() : "null") +
                "\nUser role: " + (user != null ? user.getRole() : "null");
    }
    // React Dashboard page
    @GetMapping("/react-dashboard")
    public String reactDashboard(Model model, HttpSession session) {
        User currentUser = getCurrentUser(session);

        if (currentUser == null) {
            return "redirect:/login";
        }

        model.addAttribute("user", currentUser);
        return "react-dashboard";
    }
    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }

    private User getCurrentUser(HttpSession session) {
        Long userId = (Long) session.getAttribute("loggedUserId");

        if (userId == null) {
            System.out.println("No userId in session");
            return null;
        }

        System.out.println("Fetching user with ID: " + userId);
        User user = userService.findUserById(userId);

        if (user == null) {
            System.out.println("User not found with ID: " + userId);
            return null;
        }

        return user;
    }
    @GetMapping("/test-login")
    @ResponseBody
    public String testLogin(@RequestParam String email, @RequestParam String password) {
        User user = userService.findByEmail(email);

        if (user == null) {
            return "User not found: " + email;
        }

        boolean match = userService.checkLogin(email, password);

        return "User: " + email +
                "\nFound in DB: true" +
                "\nRole: " + user.getRole() +
                "\nPassword matches: " + match +
                "\nStored hash: " + user.getPassword();
    }
//    @PostMapping("/login")
//    public String processLogin(
//            @RequestParam String email,
//            @RequestParam String password,
//            Model model,
//            HttpSession session) {
//
//        System.out.println("=== PROCESS LOGIN ===");
//
//        if (userService.checkLogin(email, password)) {
//            User user = userService.findByEmail(email);
//
//            session.setAttribute("loggedUserId", user.getId());
//
//            System.out.println("User ID stored in session: " + user.getId());
//            System.out.println("Session ID: " + session.getId());
//
//            model.addAttribute("user", user);
//            return "dashboard";
//        } else {
//            System.out.println("invalid email");
//            model.addAttribute("error", "Invalid email or password");
//            return "login";
//        }
//    }
@PostMapping("/login")
public String processLogin(
        @RequestParam String email,
        @RequestParam String password,
        Model model,
        HttpSession session) {

    System.out.println("=== PROCESS LOGIN ===");
    System.out.println("Email: " + email);

    if (userService.checkLogin(email, password)) {
        User user = userService.findByEmail(email);
        session.setAttribute("loggedUserId", user.getId());

        System.out.println("Login successful for: " + user.getEmail());
        System.out.println("User role: " + user.getRole());
        System.out.println("Redirecting to dashboard");

        return "redirect:/dashboard";
    } else {
        System.out.println("Login failed for: " + email);
        model.addAttribute("error", "Invalid email or password");
        return "login";
    }
}
    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpSession session) {

        User currentUser = getCurrentUser(session);
        if (currentUser == null) {
            return "redirect:/login";
        }
        System.out.println(" === currentuser not null "+ currentUser.getFullName());
        List<PhDRegistration> registrations = new ArrayList<>();
        model.addAttribute("user", currentUser);

        if (currentUser.getRole().equals("DOCTORANT")) {
            Campaign activeCampaign = campaignService.getCurrentActiveCampaign();
            model.addAttribute("activeCampaign", activeCampaign);

            // Calculate campaign progress percentage for countdown timer
            if (activeCampaign != null) {
                LocalDateTime now = LocalDateTime.now();
                long totalDuration = java.time.Duration.between(activeCampaign.getStartDate(), activeCampaign.getEndDate()).toMillis();
                long elapsed = java.time.Duration.between(activeCampaign.getStartDate(), now).toMillis();
                int percent = (int) Math.min(100, Math.max(0, (elapsed * 100) / totalDuration));
                model.addAttribute("campaignProgressPercent", percent);
            }

            // Get doctorant's registrations
            registrations = phdRegistrationService.getRegistrationsByDoctorant(currentUser);
            model.addAttribute("registrations", registrations);

        } else if (currentUser.getRole().equals("DIRECTOR")) {
            model.addAttribute("registrations",
                    phdRegistrationService.getRegistrationsByDirector(currentUser));
        } else if (currentUser.getRole().equals("ADMIN")) {
            model.addAttribute("registrations",
                    phdRegistrationService.getDirectorApprovedRegistrations());
        } else {
            model.addAttribute("registrations", new ArrayList<>());
        }

        return "dashboard";
    }

    @GetMapping("/admin/final-approvals")
    public String showFinalApprovals(Model model, HttpSession session) {
        User currentUser = getCurrentUser(session);
        if (currentUser == null || !currentUser.getRole().equals("ADMIN")) {
            return "redirect:/login";
        }

        model.addAttribute("user", currentUser);
        model.addAttribute("registrations", phdRegistrationService.getDirectorApprovedRegistrations());
        return "dashboard";
    }

    @GetMapping("/doctorant/register-phd")
    public String showPhDRegistrationForm(Model model, HttpSession session) {

        User currentUser = getCurrentUser(session);
        if (currentUser == null || !currentUser.getRole().equals("DOCTORANT")) {
            return "redirect:/login";
        }
        model.addAttribute("user", currentUser);
        model.addAttribute("directors", userService.findByRole("DIRECTOR"));
        model.addAttribute("registration", new PhDRegistration(currentUser, "", "", "", ""));
        return "phd-registration-form";
    }

    @PostMapping("/doctorant/register-phd")
    public String processPhDRegistration(
            @RequestParam String thesisSubject,
            @RequestParam String researchDomain,
            @RequestParam Long directorId,
            @RequestParam(required = false) String coDirectorName,
            @RequestParam("diplomaFile") MultipartFile diplomaFile,
            @RequestParam("cvFile") MultipartFile cvFile,
            @RequestParam("additionalFile") MultipartFile additionalFile,
            RedirectAttributes redirectAttributes,
            HttpSession session) {

        User currentUser = getCurrentUser(session);

        if (currentUser == null || !currentUser.getRole().equals("DOCTORANT")) {
            return "redirect:/login";
        }
        if (!campaignService.canSubmitApplications()) {
            Campaign activeCampaign = campaignService.getCurrentActiveCampaign();
            if (activeCampaign == null) {
                redirectAttributes.addFlashAttribute("error",
                        "No active registration campaign. Please contact administration.");
            } else {
                redirectAttributes.addFlashAttribute("error",
                        "Registration is currently closed. The campaign ends on: " +
                                activeCampaign.getEndDate());
            }
            return "redirect:/dashboard";
        }

        try {
            User director = userService.findUserById(directorId);
            if (director == null || !"DIRECTOR".equals(director.getRole())) {
                redirectAttributes.addFlashAttribute("error", "Selected director is invalid.");
                return "redirect:/doctorant/register-phd";
            }

            PhDRegistration registration = new PhDRegistration(
                    currentUser,
                    thesisSubject,
                    researchDomain,
                    director.getFullName().trim(),
                    coDirectorName == null ? null : coDirectorName.trim()
            );
            registration.setDirectorUserId(director.getId());

            // Save registration with files
            phdRegistrationService.saveRegistration(registration, diplomaFile, cvFile, additionalFile);

            // NOTIFY DIRECTOR
            if (director != null && director.getRole().equals("DIRECTOR")) {
                emailService.sendDirectorNotification(registration, director);
                redirectAttributes.addFlashAttribute("success",
                        "PhD registration submitted! Director has been notified.");
            } else {
                redirectAttributes.addFlashAttribute("warning",
                        "Application submitted but director email not found. Please contact administration.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
        }

        return "redirect:/dashboard";
    }
    // Director approval page
    @GetMapping("/director/review/{id}")
    public String showDirectorReview(@PathVariable Long id, Model model, HttpSession session) {
        User currentUser = getCurrentUser(session);

        if (currentUser == null || !currentUser.getRole().equals("DIRECTOR")) {
            return "redirect:/login";
        }

        PhDRegistration registration = phdRegistrationService.getRegistrationById(id);

        boolean isOwnerById = registration != null &&
                registration.getDirectorUserId() != null &&
                registration.getDirectorUserId().equals(currentUser.getId());
        boolean isOwnerByName = registration != null &&
                registration.getDirectorName() != null &&
                currentUser.getFullName() != null &&
                registration.getDirectorName().trim().equalsIgnoreCase(currentUser.getFullName().trim());

        if (registration == null || (!isOwnerById && !isOwnerByName)) {
            return "redirect:/dashboard";
        }

        model.addAttribute("registration", registration);
        model.addAttribute("user", currentUser);
        return "director-review";
    }

    // Process director approval
    @PostMapping("/director/approve/{id}")
    public String directorApprove(@PathVariable Long id,
                                  @RequestParam String decision,
                                  @RequestParam String comments,
                                  RedirectAttributes redirectAttributes,
                                  HttpSession session) {

        User currentUser = getCurrentUser(session);

        if (currentUser == null || !currentUser.getRole().equals("DIRECTOR")) {
            return "redirect:/login";
        }

        try {
            if ("approve".equalsIgnoreCase(decision)) {
                PhDRegistration updated = phdRegistrationService.directorApprove(id, comments);
                if (updated == null) {
                    redirectAttributes.addFlashAttribute("error", "Unable to approve this application.");
                } else {
                    redirectAttributes.addFlashAttribute("success", "Application approved! Admin notified.");
                }
            } else if ("reject".equalsIgnoreCase(decision)) {
                PhDRegistration updated = phdRegistrationService.directorReject(id, comments);
                if (updated == null) {
                    redirectAttributes.addFlashAttribute("error", "Unable to reject this application.");
                } else {
                    redirectAttributes.addFlashAttribute("success", "Application rejected. Student notified.");
                }
            } else {
                redirectAttributes.addFlashAttribute("error", "Invalid decision selected.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error while processing decision: " + e.getMessage());
        }

        return "redirect:/dashboard";
    }

    // Admin approval page
    @GetMapping("/admin/review/{id}")
    public String showAdminReview(@PathVariable Long id, Model model, HttpSession session) {
        User currentUser = getCurrentUser(session);

        if (currentUser == null || !currentUser.getRole().equals("ADMIN")) {
            return "redirect:/login";
        }

        PhDRegistration registration = phdRegistrationService.getRegistrationById(id);

        if (registration == null || !registration.getStatus().equals("DIRECTOR_APPROVED")) {
            return "redirect:/dashboard";
        }

        model.addAttribute("registration", registration);
        model.addAttribute("user", currentUser);
        return "admin-review";
    }

    // Process admin final approval
    @PostMapping("/admin/approve/{id}")
    public String adminApprove(@PathVariable Long id,
                               @RequestParam String decision,
                               @RequestParam String comments,
                               RedirectAttributes redirectAttributes,
                               HttpSession session) {

        User currentUser = getCurrentUser(session);

        if (currentUser == null || !currentUser.getRole().equals("ADMIN")) {
            return "redirect:/login";
        }

        try {
            if ("approve".equalsIgnoreCase(decision)) {
                PhDRegistration updated = phdRegistrationService.adminApprove(id, comments);
                if (updated == null) {
                    redirectAttributes.addFlashAttribute("error", "Unable to approve this application.");
                } else {
                    redirectAttributes.addFlashAttribute("success", "Application FINALLY APPROVED! Student notified.");
                }
            } else if ("reject".equalsIgnoreCase(decision)) {
                PhDRegistration updated = phdRegistrationService.adminReject(id, comments);
                if (updated == null) {
                    redirectAttributes.addFlashAttribute("error", "Unable to reject this application.");
                } else {
                    redirectAttributes.addFlashAttribute("success", "Application rejected. Student notified.");
                }
            } else {
                redirectAttributes.addFlashAttribute("error", "Invalid decision selected.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error while processing decision: " + e.getMessage());
        }

        return "redirect:/dashboard";
    }

    @GetMapping("/registration/{id}")
    public String viewRegistration(@PathVariable Long id, Model model, HttpSession session) {
        PhDRegistration registration = phdRegistrationService.getRegistrationById(id);
        User currentUser = getCurrentUser(session);
        if (registration == null) {
            return "redirect:/dashboard";
        }

        // Check permission
        if (currentUser.getRole().equals("DOCTORANT") && !registration.getDoctorant().getId().equals(currentUser.getId())) {
            return "redirect:/dashboard";
        }

        model.addAttribute("registration", registration);
        model.addAttribute("user", currentUser);
        return "registration-details";
    }

    @PostMapping("/registration/{id}/approve")
    public String approveRegistration(@PathVariable Long id,
                                      @RequestParam String action,
                                      @RequestParam String feedback,
                                      RedirectAttributes redirectAttributes, HttpSession session) {
        User currentUser = getCurrentUser(session);
        String status = action.equals("approve") ? "APPROVED" : "REJECTED";
        String approverRole = currentUser.getRole();

        phdRegistrationService.updateStatus(id, status, feedback, approverRole);
        redirectAttributes.addFlashAttribute("success", "Registration " + status.toLowerCase() + "!");

        return "redirect:/dashboard";
    }

    @GetMapping("/doctorant/prerequisites")
    public String showPrerequisites(Model model, HttpSession session) {
        User currentUser = getCurrentUser(session);

        if (currentUser == null || !currentUser.getRole().equals("DOCTORANT")) {
            return "redirect:/login";
        }

        ProgressDTO progress = prerequisiteService.calculateProgress(currentUser);
        List<Publication> publications = prerequisiteService.getPublicationsByDoctorant(currentUser);
        List<Training> trainings = prerequisiteService.getTrainingsByDoctorant(currentUser);

        model.addAttribute("user", currentUser);
        model.addAttribute("progress", progress);
        model.addAttribute("publications", publications);
        model.addAttribute("trainings", trainings);

        return "prerequisites-dashboard";
    }
    @GetMapping("/doctorant/add-publication")
    public String showAddPublicationForm(Model model, HttpSession session) {
        User currentUser = getCurrentUser(session);

        if (currentUser == null || !currentUser.getRole().equals("DOCTORANT")) {
            return "redirect:/login";
        }

        model.addAttribute("user", currentUser);
        model.addAttribute("publication", new Publication());
        return "add-publication";
    }

    // Process add publication
    @PostMapping("/doctorant/add-publication")
    public String addPublication(
            @RequestParam String title,
            @RequestParam String journalOrConferenceName,
            @RequestParam String type,
            @RequestParam(required = false) String quartile,
            @RequestParam(required = false) String publicationDate,
            RedirectAttributes redirectAttributes,
            HttpSession session) {

        User currentUser = getCurrentUser(session);

        if (currentUser == null || !currentUser.getRole().equals("DOCTORANT")) {
            return "redirect:/login";
        }

        try {
            LocalDate date = (publicationDate != null && !publicationDate.isEmpty())
                    ? LocalDate.parse(publicationDate) : LocalDate.now();

            Publication publication = new Publication(currentUser, title, journalOrConferenceName,
                    type, quartile, date);
            prerequisiteService.addPublication(publication);
            redirectAttributes.addFlashAttribute("success", "Publication added successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error adding publication: " + e.getMessage());
        }

        return "redirect:/doctorant/prerequisites";
    }

    // Show add training form
    @GetMapping("/doctorant/add-training")
    public String showAddTrainingForm(Model model, HttpSession session) {
        User currentUser = getCurrentUser(session);

        if (currentUser == null || !currentUser.getRole().equals("DOCTORANT")) {
            return "redirect:/login";
        }

        model.addAttribute("user", currentUser);
        model.addAttribute("training", new Training());
        return "add-training";
    }

    // Process add training
    @PostMapping("/doctorant/add-training")
    public String addTraining(
            @RequestParam String courseName,
            @RequestParam Integer hours,
            @RequestParam(required = false) String provider,
            @RequestParam(required = false) String completionDate,
            RedirectAttributes redirectAttributes,
            HttpSession session) {

        User currentUser = getCurrentUser(session);

        if (currentUser == null || !currentUser.getRole().equals("DOCTORANT")) {
            return "redirect:/login";
        }

        try {
            LocalDate date = (completionDate != null && !completionDate.isEmpty())
                    ? LocalDate.parse(completionDate) : LocalDate.now();

            Training training = new Training(currentUser, courseName, hours, provider, date);
            prerequisiteService.addTraining(training);
            redirectAttributes.addFlashAttribute("success", "Training added successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error adding training: " + e.getMessage());
        }

        return "redirect:/doctorant/prerequisites";
    }

    // Delete publication
    @GetMapping("/doctorant/delete-publication/{id}")
    public String deletePublication(@PathVariable Long id, RedirectAttributes redirectAttributes, HttpSession session) {
        User currentUser = getCurrentUser(session);

        if (currentUser == null) {
            return "redirect:/login";
        }

        prerequisiteService.deletePublication(id);
        redirectAttributes.addFlashAttribute("success", "Publication deleted!");
        return "redirect:/doctorant/prerequisites";
    }

    // Delete training
    @GetMapping("/doctorant/delete-training/{id}")
    public String deleteTraining(@PathVariable Long id, RedirectAttributes redirectAttributes, HttpSession session) {
        User currentUser = getCurrentUser(session);

        if (currentUser == null) {
            return "redirect:/login";
        }

        prerequisiteService.deleteTraining(id);
        redirectAttributes.addFlashAttribute("success", "Training deleted!");
        return "redirect:/doctorant/prerequisites";
    }
    @GetMapping("/admin/campaigns")
    public String showCampaigns(Model model, HttpSession session) {
        User currentUser = getCurrentUser(session);

        if (currentUser == null || !currentUser.getRole().equals("ADMIN")) {
            return "redirect:/login";
        }

        List<Campaign> campaigns = campaignService.getAllCampaigns();
        model.addAttribute("user", currentUser);
        model.addAttribute("campaigns", campaigns);
        return "admin-campaigns";
    }
    @GetMapping("/admin/campaigns/create")
    public String showCreateCampaignForm(Model model, HttpSession session) {
        User currentUser = getCurrentUser(session);

        if (currentUser == null || !currentUser.getRole().equals("ADMIN")) {
            return "redirect:/login";
        }

        model.addAttribute("user", currentUser);
        model.addAttribute("campaign", new Campaign());
        return "admin-campaign-form";
    }
    @PostMapping("/admin/campaigns/create")
    public String createCampaign(
            @RequestParam String name,
            @RequestParam String description,
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(required = false) Integer maxApplications,
            RedirectAttributes redirectAttributes,
            HttpSession session) {

        User currentUser = getCurrentUser(session);

        if (currentUser == null || !currentUser.getRole().equals("ADMIN")) {
            return "redirect:/login";
        }

        try {
            LocalDateTime start = LocalDate.parse(startDate, DateTimeFormatter.ISO_LOCAL_DATE).atStartOfDay();
            LocalDateTime end = LocalDate.parse(endDate, DateTimeFormatter.ISO_LOCAL_DATE).atTime(23, 59, 59);

            if (!end.isAfter(start)) {
                redirectAttributes.addFlashAttribute("error", "End date must be after start date.");
                return "redirect:/admin/campaigns/create";
            }

            Campaign campaign = new Campaign(name, description, start, end);
            campaign.setMaxApplications(maxApplications);
            campaignService.createCampaign(campaign);

            redirectAttributes.addFlashAttribute("success", "Campaign created successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error creating campaign: " + e.getMessage());
        }

        return "redirect:/admin/campaigns";
    }
    @GetMapping("/admin/campaigns/edit/{id}")
    public String showEditCampaignForm(@PathVariable Long id, Model model, HttpSession session) {
        User currentUser = getCurrentUser(session);

        if (currentUser == null || !currentUser.getRole().equals("ADMIN")) {
            return "redirect:/login";
        }

        Campaign campaign = campaignService.getCampaignById(id);
        if (campaign == null) {
            return "redirect:/admin/campaigns";
        }

        model.addAttribute("user", currentUser);
        model.addAttribute("campaign", campaign);
        return "admin-campaign-edit";
    }
    @PostMapping("/admin/campaigns/edit/{id}")
    public String updateCampaign(
            @PathVariable Long id,
            @RequestParam String name,
            @RequestParam String description,
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam String status,
            @RequestParam(required = false) Integer maxApplications,
            RedirectAttributes redirectAttributes,
            HttpSession session) {

        User currentUser = getCurrentUser(session);

        if (currentUser == null || !currentUser.getRole().equals("ADMIN")) {
            return "redirect:/login";
        }

        try {
            LocalDateTime start = LocalDate.parse(startDate, DateTimeFormatter.ISO_LOCAL_DATE).atStartOfDay();
            LocalDateTime end = LocalDate.parse(endDate, DateTimeFormatter.ISO_LOCAL_DATE).atTime(23, 59, 59);

            if (!end.isAfter(start)) {
                redirectAttributes.addFlashAttribute("error", "End date must be after start date.");
                return "redirect:/admin/campaigns/edit/" + id;
            }

            Campaign campaign = new Campaign(name, description, start, end);
            campaign.setStatus(status);
            campaign.setMaxApplications(maxApplications);

            campaignService.updateCampaign(id, campaign);
            redirectAttributes.addFlashAttribute("success", "Campaign updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating campaign: " + e.getMessage());
        }

        return "redirect:/admin/campaigns";
    }
    @GetMapping("/admin/campaigns/delete/{id}")
    public String deleteCampaign(@PathVariable Long id, RedirectAttributes redirectAttributes, HttpSession session) {
        User currentUser = getCurrentUser(session);

        if (currentUser == null || !currentUser.getRole().equals("ADMIN")) {
            return "redirect:/login";
        }

        campaignService.deleteCampaign(id);
        redirectAttributes.addFlashAttribute("success", "Campaign deleted!");
        return "redirect:/admin/campaigns";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }



}
