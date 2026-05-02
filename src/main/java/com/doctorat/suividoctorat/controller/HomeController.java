package com.doctorat.suividoctorat.controller;

import com.doctorat.suividoctorat.entity.PhDRegistration;
import com.doctorat.suividoctorat.entity.User;
import com.doctorat.suividoctorat.service.PhDRegistrationService;
import com.doctorat.suividoctorat.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

import com.doctorat.suividoctorat.dto.ProgressDTO;
import com.doctorat.suividoctorat.entity.Publication;
import com.doctorat.suividoctorat.entity.Training;
import com.doctorat.suividoctorat.service.PrerequisiteService;


import java.util.ArrayList;
import java.util.List;

@Controller
public class HomeController {

    @Autowired
    private UserService userService;

    @Autowired
    private PhDRegistrationService phdRegistrationService;

    @Autowired
    private PrerequisiteService prerequisiteService;

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
    @PostMapping("/login")
    public String processLogin(
            @RequestParam String email,
            @RequestParam String password,
            Model model,
            HttpSession session) {

        System.out.println("=== PROCESS LOGIN ===");

        if (userService.checkLogin(email, password)) {
            User user = userService.findByEmail(email);

            session.setAttribute("loggedUserId", user.getId());

            System.out.println("User ID stored in session: " + user.getId());
            System.out.println("Session ID: " + session.getId());

            model.addAttribute("user", user);
            return "dashboard";
        } else {
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

        //  based on role
        if (currentUser.getRole().equals("DOCTORANT")) {
            registrations = phdRegistrationService.getRegistrationsByDoctorant(currentUser);
            model.addAttribute("registrations", registrations);
        } else if (currentUser.getRole().equals("DIRECTOR")) {
            model.addAttribute("registrations",
                    phdRegistrationService.getRegistrationsByDirector(currentUser.getFullName()));
        } else if (currentUser.getRole().equals("ADMIN")) {
            model.addAttribute("registrations",
                    phdRegistrationService.getPendingRegistrations());
        }
        else{
            model.addAttribute("registrations", new ArrayList<>());
        }

        return "dashboard";
    }

    @GetMapping("/doctorant/register-phd")
    public String showPhDRegistrationForm(Model model, HttpSession session) {

        User currentUser = getCurrentUser(session);
        if (currentUser == null || !currentUser.getRole().equals("DOCTORANT")) {
            return "redirect:/login";
        }
        model.addAttribute("registration", new PhDRegistration(currentUser, "", "", "", ""));
        return "phd-registration-form";
    }

    @PostMapping("/doctorant/register-phd")
    public String processPhDRegistration(
            @RequestParam String thesisSubject,
            @RequestParam String researchDomain,
            @RequestParam String directorName,
            @RequestParam(required = false) String coDirectorName,
            @RequestParam("diplomaFile") MultipartFile diplomaFile,
            @RequestParam("cvFile") MultipartFile cvFile,
            @RequestParam("additionalFile") MultipartFile additionalFile,
            @RequestParam(required = false) String sessionId,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request,
            HttpSession session) {


        System.out.println("=== PROCESSING PHD REGISTRATION ===");
        System.out.println("Current session ID: " + session.getId());
        System.out.println("Received session ID: " + sessionId);
        System.out.println("Is session new" + session.isNew());

        User currentUser = getCurrentUser(session);


        if (currentUser == null && sessionId != null) {
            System.out.println("Session lost! Attempting recovery...");

        }

        if (currentUser == null) {
            System.out.println("ERROR: No user in session!");
            redirectAttributes.addFlashAttribute("error", "Your session expired. Please login again.");
            return "redirect:/login";
        }

        if (!currentUser.getRole().equals("DOCTORANT")) {
            System.out.println("ERROR: User is not a DOCTORANT. Role: " + currentUser.getRole());
            return "redirect:/login";
        }

        System.out.println("User found: " + currentUser.getEmail());

        try {
            PhDRegistration registration = new PhDRegistration(
                    currentUser, thesisSubject, researchDomain, directorName, coDirectorName
            );

            phdRegistrationService.saveRegistration(registration, diplomaFile, cvFile, additionalFile);
            redirectAttributes.addFlashAttribute("success", "PhD registration submitted successfully!");
            System.out.println("SUCCESS: Registration saved");

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error submitting registration: " + e.getMessage());
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

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }



}