package com.doctorat.suividoctorat.service;

import com.doctorat.suividoctorat.entity.User;
import com.doctorat.suividoctorat.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // Register a new user
    public String registerUser(String email, String password, String fullName, String role) {

        System.out.println("=== REGISTRATION ATTEMPT ===");
        System.out.println("Email: " + email);
        System.out.println("Full Name: " + fullName);
        System.out.println("Role: " + role);

        try {
            if (userRepository.existsByEmail(email)) {
                System.out.println("ERROR: Email already exists!");
                return "Email already registered!";
            }
            // ENCRYPT the password
            String encryptedPassword = passwordEncoder.encode(password);
            System.out.println(" password before encryption : " + password);
            System.out.println("Encrypted password: " + encryptedPassword);
            // save
            User user = new User(email, encryptedPassword, fullName, role);
            userRepository.save(user);

            System.out.println("=== SUCCESS: User saved with ID: " + user.getId()+" ==");

        } catch (Exception e) {
            System.out.println("EXCEPTION: " + e.getMessage());
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }

        return "Registration successful!";
    }
    public User findUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }
    // Find user by email
    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }
    public User findByFullName(String fullName) {
        return userRepository.findByFullName(fullName).orElse(null);
    }
    public boolean checkLogin(String email, String rawPassword) {
        User user = findByEmail(email);
        if (user == null) {
            System.out.println("User not found: " + email);
            return false;
        }

        boolean matches = passwordEncoder.matches(rawPassword, user.getPassword());
        System.out.println("Password matches: " + matches);
        return matches;
    }

}