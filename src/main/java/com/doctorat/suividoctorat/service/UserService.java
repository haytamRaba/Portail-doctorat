package com.doctorat.suividoctorat.service;

import com.doctorat.suividoctorat.entity.User;
import com.doctorat.suividoctorat.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // Register a new user
    public String registerUser(String email, String password, String fullName, String role) {

        // Check if email already exists
        if (userRepository.existsByEmail(email)) {
            return "Email already registered!";
        }

        // Create new user
        User user = new User(email, password, fullName, role);

        // Save to database
        userRepository.save(user);

        return "Registration successful!";
    }

    // Find user by email
    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }
}