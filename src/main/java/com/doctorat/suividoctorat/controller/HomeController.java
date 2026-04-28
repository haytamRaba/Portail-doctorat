package com.doctorat.suividoctorat.controller;

import com.doctorat.suividoctorat.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {

    @Autowired
    private UserService userService;

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

        String result = userService.registerUser(email, password, fullName, role);

        if (result.equals("Registration successful!")) {
            model.addAttribute("success", "Account created! Please login.");
            return "login";
        } else {
            model.addAttribute("error", result);
            return "register";
        }
    }


    @GetMapping("/login/test/{id}")
    public String testId(Model model){
        String myMessage="testo testing my test";
        model.addAttribute("testop", myMessage);

        return "testo";
    }

    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard";
    }
}