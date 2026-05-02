package com.doctorat.suividoctorat.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "trainings")
public class Training {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "doctorant_id", nullable = false)
    private User doctorant;

    @Column(nullable = false)
    private String courseName;

    @Column(nullable = false)
    private Integer hours;  // Number of training hours

    private String provider;  // Who provided the training

    private LocalDate completionDate;

    private String certificatePath;  // Path to uploaded certificate

    private LocalDateTime createdAt;

    private String status;  // "PENDING", "VERIFIED", "REJECTED"

    // Constructors
    public Training() {}

    public Training(User doctorant, String courseName, Integer hours, String provider, LocalDate completionDate) {
        this.doctorant = doctorant;
        this.courseName = courseName;
        this.hours = hours;
        this.provider = provider;
        this.completionDate = completionDate;
        this.createdAt = LocalDateTime.now();
        this.status = "PENDING";
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getDoctorant() { return doctorant; }
    public void setDoctorant(User doctorant) { this.doctorant = doctorant; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public Integer getHours() { return hours; }
    public void setHours(Integer hours) { this.hours = hours; }

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }

    public LocalDate getCompletionDate() { return completionDate; }
    public void setCompletionDate(LocalDate completionDate) { this.completionDate = completionDate; }

    public String getCertificatePath() { return certificatePath; }
    public void setCertificatePath(String certificatePath) { this.certificatePath = certificatePath; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
