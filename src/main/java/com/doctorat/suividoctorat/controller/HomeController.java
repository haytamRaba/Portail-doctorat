package com.doctorat.suividoctorat.controller;

import com.doctorat.suividoctorat.entity.PhDRegistration;
import com.doctorat.suividoctorat.entity.User;
import com.doctorat.suividoctorat.service.PhDRegistrationService;
import com.doctorat.suividoctorat.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class HomeController {

    @Autowired
    private UserService userService;

    @Autowired
    private PhDRegistrationService phdRegistrationService;

    private User currentUser;

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


//    @GetMapping("/login/test/{id}")
//    public String testId(Model model){
//        String myMessage="testo testing my test";
//        model.addAttribute("testop", myMessage);
//
//        return "testo";
//    }

    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }

    @PostMapping("/login")
    public String processLogin(
            @RequestParam String email,
            @RequestParam String password,
            Model model) {


        if (userService.checkLogin(email, password)) {
            System.out.print(".");
            User user = userService.findByEmail(email);
            System.out.print(".");
            model.addAttribute("user", user);
            System.out.print(".");
            System.out.println("== cureent user before redirect to dashboard : "+user.getFullName());

            return "dashboard";
        } else {
            model.addAttribute("error", "Invalid email or password");
            return "login";
        }
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        if (currentUser == null) {
            return "redirect:/login";
        }
        System.out.println(" === currentuser not null "+ currentUser.getFullName());

        model.addAttribute("user", currentUser);

        // Show registrations based on role
        if (currentUser.getRole().equals("DOCTORANT")) {
            model.addAttribute("registrations",
                    phdRegistrationService.getRegistrationsByDoctorant(currentUser));
        } else if (currentUser.getRole().equals("DIRECTOR")) {
            model.addAttribute("registrations",
                    phdRegistrationService.getRegistrationsByDirector(currentUser.getFullName()));
        } else if (currentUser.getRole().equals("ADMIN")) {
            model.addAttribute("registrations",
                    phdRegistrationService.getPendingRegistrations());
        }

        return "dashboard";
    }

    @GetMapping("/doctorant/register-phd")
    public String showPhDRegistrationForm(Model model) {
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
            RedirectAttributes redirectAttributes) {

        if (currentUser == null || !currentUser.getRole().equals("DOCTORANT")) {
            return "redirect:/login";
        }

        try {
            PhDRegistration registration = new PhDRegistration(
                    currentUser, thesisSubject, researchDomain, directorName, coDirectorName
            );

            phdRegistrationService.saveRegistration(registration, diplomaFile, cvFile, additionalFile);
            redirectAttributes.addFlashAttribute("success", "PhD registration submitted successfully!");

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error submitting registration: " + e.getMessage());
        }

        return "redirect:/dashboard";
    }

    @GetMapping("/registration/{id}")
    public String viewRegistration(@PathVariable Long id, Model model) {
        PhDRegistration registration = phdRegistrationService.getRegistrationById(id);

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
                                      RedirectAttributes redirectAttributes) {

        String status = action.equals("approve") ? "APPROVED" : "REJECTED";
        String approverRole = currentUser.getRole();

        phdRegistrationService.updateStatus(id, status, feedback, approverRole);
        redirectAttributes.addFlashAttribute("success", "Registration " + status.toLowerCase() + "!");

        return "redirect:/dashboard";
    }





}