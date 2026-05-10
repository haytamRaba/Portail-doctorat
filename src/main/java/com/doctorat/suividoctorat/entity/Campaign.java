package com.doctorat.suividoctorat.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "campaigns")
public class Campaign {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private LocalDateTime startDate;

    @Column(nullable = false)
    private LocalDateTime endDate;

    private String status;  // "ACTIVE", "UPCOMING", "CLOSED"

    private Integer maxApplications;  // Maximum number of applications allowed

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public Campaign() {}

    public Campaign(String name, String description, LocalDateTime startDate, LocalDateTime endDate) {
        this.name = name;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = "UPCOMING";
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }

    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getMaxApplications() { return maxApplications; }
    public void setMaxApplications(Integer maxApplications) { this.maxApplications = maxApplications; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // Helper methods
    public boolean isActive() {
        LocalDateTime now = LocalDateTime.now();
        return "ACTIVE".equals(status) &&
                now.isAfter(startDate) &&
                now.isBefore(endDate);
    }

    public boolean isUpcoming() {
        LocalDateTime now = LocalDateTime.now();
        return now.isBefore(startDate);
    }

    public boolean isClosed() {
        LocalDateTime now = LocalDateTime.now();
        return "CLOSED".equals(status) || now.isAfter(endDate);
    }

    public String getTimeRemaining() {
        if (!isActive()) return "Not active";

        LocalDateTime now = LocalDateTime.now();
        long days = java.time.Duration.between(now, endDate).toDays();
        long hours = java.time.Duration.between(now, endDate).toHours() % 24;

        if (days > 0) {
            return days + " days and " + hours + " hours";
        } else if (hours > 0) {
            return hours + " hours";
        } else {
            long minutes = java.time.Duration.between(now, endDate).toMinutes() % 60;
            return minutes + " minutes";
        }
    }
}