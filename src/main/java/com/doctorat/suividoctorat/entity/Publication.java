package com.doctorat.suividoctorat.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "publications")
public class Publication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "doctorant_id", nullable = false)
    private User doctorant;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String journalOrConferenceName;

    @Column(nullable = false)
    private String type;  // "ARTICLE" or "CONFERENCE"

    private String quartile;  // "Q1", "Q2", "Q3", "Q4" (for articles only)

    private LocalDate publicationDate;

    private String doi;  // Digital Object Identifier (optional)

    private String authors;

    private LocalDateTime createdAt;

    private String status;  // "PENDING", "VERIFIED", "REJECTED"

    // Constructors
    public Publication() {}

    public Publication(User doctorant, String title, String journalOrConferenceName,
                       String type, String quartile, LocalDate publicationDate) {
        this.doctorant = doctorant;
        this.title = title;
        this.journalOrConferenceName = journalOrConferenceName;
        this.type = type;
        this.quartile = quartile;
        this.publicationDate = publicationDate;
        this.createdAt = LocalDateTime.now();
        this.status = "PENDING";
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getDoctorant() { return doctorant; }
    public void setDoctorant(User doctorant) { this.doctorant = doctorant; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getJournalOrConferenceName() { return journalOrConferenceName; }
    public void setJournalOrConferenceName(String journalOrConferenceName) {
        this.journalOrConferenceName = journalOrConferenceName;
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getQuartile() { return quartile; }
    public void setQuartile(String quartile) { this.quartile = quartile; }

    public LocalDate getPublicationDate() { return publicationDate; }
    public void setPublicationDate(LocalDate publicationDate) { this.publicationDate = publicationDate; }

    public String getDoi() { return doi; }
    public void setDoi(String doi) { this.doi = doi; }

    public String getAuthors() { return authors; }
    public void setAuthors(String authors) { this.authors = authors; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}