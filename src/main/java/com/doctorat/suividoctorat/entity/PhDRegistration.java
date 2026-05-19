package com.doctorat.suividoctorat.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "phd_registrations")
public class PhDRegistration {
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_DIRECTOR_APPROVED = "DIRECTOR_APPROVED";
    public static final String STATUS_DIRECTOR_REJECTED = "DIRECTOR_REJECTED";
    public static final String STATUS_ADMIN_APPROVED = "APPROVED";
    public static final String STATUS_ADMIN_REJECTED = "REJECTED";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "doctorant_id", nullable = false)
    private User doctorant;

    @Column(nullable = false, length = 500)
    private String thesisSubject;

    @Column(nullable = false)
    private String researchDomain;

    @Column(nullable = false)
    private String directorName;

    private Long directorUserId;

    private String coDirectorName;

    private String diplomaFilePath;
    private String cvFilePath;
    private String additionalDocumentPath;

    private String status; // PENDING, DIRECTOR_APPROVED, ADMIN_APPROVED, REJECTED

    @Column(columnDefinition = "TEXT")
    private String directorFeedback;
    @Column(columnDefinition = "TEXT")
    private String adminFeedback;

    // Timestamps
    private LocalDateTime submissionDate;
    private LocalDateTime directorApprovalDate;
    private LocalDateTime adminApprovalDate;

    public PhDRegistration() {}

    public PhDRegistration(User doctorant, String thesisSubject, String researchDomain,
                           String directorName, String coDirectorName) {
        this.doctorant = doctorant;
        this.thesisSubject = thesisSubject;
        this.researchDomain = researchDomain;
        this.directorName = directorName;
        this.coDirectorName = coDirectorName;
        this.status = "PENDING";
        this.submissionDate = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    //doctorant
    public User getDoctorant() { return doctorant; }
    public void setDoctorant(User doctorant) { this.doctorant = doctorant; }
    // thesis subject
    public String getThesisSubject() { return thesisSubject; }
    public void setThesisSubject(String thesisSubject) { this.thesisSubject = thesisSubject; }
    //domain research
    public String getResearchDomain() { return researchDomain; }
    public void setResearchDomain(String researchDomain) { this.researchDomain = researchDomain; }
    // == director name and co
    public String getDirectorName() { return directorName; }
    public void setDirectorName(String directorName) { this.directorName = directorName; }

    public Long getDirectorUserId() { return directorUserId; }
    public void setDirectorUserId(Long directorUserId) { this.directorUserId = directorUserId; }

    public String getCoDirectorName() { return coDirectorName; }
    public void setCoDirectorName(String coDirectorName) { this.coDirectorName = coDirectorName; }
   // == infomations ========
    public String getDiplomaFilePath() { return diplomaFilePath; }
    public void setDiplomaFilePath(String diplomaFilePath) { this.diplomaFilePath = diplomaFilePath; }

    public String getCvFilePath() { return cvFilePath; }
    public void setCvFilePath(String cvFilePath) { this.cvFilePath = cvFilePath; }

    public String getAdditionalDocumentPath() { return additionalDocumentPath; }
    public void setAdditionalDocumentPath(String additionalDocumentPath) { this.additionalDocumentPath = additionalDocumentPath; }

    //=== status ===
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    // ==== Feedback ================
    public String getDirectorFeedback() { return directorFeedback; }
    public void setDirectorFeedback(String directorFeedback) { this.directorFeedback = directorFeedback; }

    public String getAdminFeedback() { return adminFeedback; }
    public void setAdminFeedback(String adminFeedback) { this.adminFeedback = adminFeedback; }
    // ===== dates =======
    public LocalDateTime getSubmissionDate() { return submissionDate; }
    public void setSubmissionDate(LocalDateTime submissionDate) { this.submissionDate = submissionDate; }

    public LocalDateTime getDirectorApprovalDate() { return directorApprovalDate; }
    public void setDirectorApprovalDate(LocalDateTime directorApprovalDate) { this.directorApprovalDate = directorApprovalDate; }

    public LocalDateTime getAdminApprovalDate() { return adminApprovalDate; }
    public void setAdminApprovalDate(LocalDateTime adminApprovalDate) { this.adminApprovalDate = adminApprovalDate; }
}
