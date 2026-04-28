package com.doctorat.suividoctorat.repository;

import com.doctorat.suividoctorat.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Spring Boot automatically implements:
    Optional<User> findByEmail(String email);  // Find user by email

    boolean existsByEmail(String email);  // Check if email already registered
}