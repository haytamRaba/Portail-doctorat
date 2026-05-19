package com.doctorat.suividoctorat.service;

import com.doctorat.suividoctorat.entity.PhDRegistration;
import com.doctorat.suividoctorat.entity.User;
import com.doctorat.suividoctorat.repository.PhDRegistrationRepository;
import com.doctorat.suividoctorat.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class PhDRegistrationService {

    @Autowired
    private PhDRegistrationRepository registrationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Value("${file.upload-dir}")
    private String uploadDir;

    // Save PhD registration with files
    public PhDRegistration saveRegistration(PhDRegistration registration,
                                            MultipartFile diplomaFile,
                                            MultipartFile cvFile,
                                            MultipartFile additionalFile) throws IOException {

        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        if (diplomaFile != null && !diplomaFile.isEmpty()) {
            String diplomaFileName = saveFile(diplomaFile, "diploma_");
            registration.setDiplomaFilePath(diplomaFileName);
        }

        if (cvFile != null && !cvFile.isEmpty()) {
            String cvFileName = saveFile(cvFile, "cv_");
            registration.setCvFilePath(cvFileName);
        }

        if (additionalFile != null && !additionalFile.isEmpty()) {
            String additionalFileName = saveFile(additionalFile, "additional_");
            registration.setAdditionalDocumentPath(additionalFileName);
        }

        registration.setSubmissionDate(LocalDateTime.now());
        registration.setStatus(PhDRegistration.STATUS_PENDING);

        return registrationRepository.save(registration);
    }

    private String saveFile(MultipartFile file, String prefix) throws IOException {
        String originalFileName = file.getOriginalFilename();
        String fileExtension = "";

        if (originalFileName != null && originalFileName.contains(".")) {
            fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }

        String uniqueFileName = prefix + UUID.randomUUID().toString() + fileExtension;
        Path filePath = Paths.get(uploadDir, uniqueFileName);
        Files.write(filePath, file.getBytes());

        return uniqueFileName;
    }

    public List<PhDRegistration> getRegistrationsByDoctorant(User doctorant) {
        return registrationRepository.findByDoctorant(doctorant);
    }

    public PhDRegistration getRegistrationById(Long id) {
        return registrationRepository.findById(id).orElse(null);
    }

    // Update registration status (for director/admin approval)
    public PhDRegistration updateStatus(Long id, String status, String feedback, String approverRole) {
        PhDRegistration registration = getRegistrationById(id);
        if (registration != null) {
            registration.setStatus(status);

            if (approverRole.equals("DIRECTOR")) {
                registration.setDirectorFeedback(feedback);
                registration.setDirectorApprovalDate(LocalDateTime.now());
            } else if (approverRole.equals("ADMIN")) {
                registration.setAdminFeedback(feedback);
                registration.setAdminApprovalDate(LocalDateTime.now());
            }

            return registrationRepository.save(registration);
        }
        return null;
    }

    // Get all pending registrations (for admin/director)
    public List<PhDRegistration> getPendingRegistrations() {
        return registrationRepository.findByStatus(PhDRegistration.STATUS_PENDING);
    }

    public List<PhDRegistration> getRegistrationsByDirector(User director) {
        if (director == null) {
            return List.of();
        }

        List<PhDRegistration> byId = registrationRepository.findByDirectorUserId(director.getId());
        List<PhDRegistration> byName = registrationRepository.findByDirectorNameNormalized(director.getFullName().trim());

        List<PhDRegistration> merged = new ArrayList<>(byId);
        for (PhDRegistration reg : byName) {
            if (merged.stream().noneMatch(r -> r.getId().equals(reg.getId()))) {
                merged.add(reg);
            }
        }
        return merged;
    }

    // ===== APPROVAL WORKFLOW METHODS =====

    // Director approves application
    public PhDRegistration directorApprove(Long id, String feedback) {
        PhDRegistration registration = getRegistrationById(id);
        if (registration != null && registration.getStatus().equals(PhDRegistration.STATUS_PENDING)) {
            registration.setStatus(PhDRegistration.STATUS_DIRECTOR_APPROVED);
            registration.setDirectorFeedback(feedback);
            registration.setDirectorApprovalDate(LocalDateTime.now());

            PhDRegistration saved = registrationRepository.save(registration);

            // Send email to student
            emailService.sendDirectorDecisionNotification(registration, "APPROVED", feedback);

            // Find admin users and notify them
            List<User> admins = userRepository.findByRole("ADMIN");
            for (User admin : admins) {
                emailService.sendAdminNotification(registration, admin);
            }

            return saved;
        }
        return null;
    }

    // Director rejects application
    public PhDRegistration directorReject(Long id, String feedback) {
        PhDRegistration registration = getRegistrationById(id);
        if (registration != null && registration.getStatus().equals(PhDRegistration.STATUS_PENDING)) {
            registration.setStatus(PhDRegistration.STATUS_DIRECTOR_REJECTED);
            registration.setDirectorFeedback(feedback);
            registration.setDirectorApprovalDate(LocalDateTime.now());

            PhDRegistration saved = registrationRepository.save(registration);

            // Send email to student
            emailService.sendDirectorDecisionNotification(registration, "REJECTED", feedback);

            return saved;
        }
        return null;
    }

    // Admin final approval
    public PhDRegistration adminApprove(Long id, String feedback) {
        PhDRegistration registration = getRegistrationById(id);
        if (registration != null && registration.getStatus().equals(PhDRegistration.STATUS_DIRECTOR_APPROVED)) {
            registration.setStatus(PhDRegistration.STATUS_ADMIN_APPROVED);
            registration.setAdminFeedback(feedback);
            registration.setAdminApprovalDate(LocalDateTime.now());

            PhDRegistration saved = registrationRepository.save(registration);

            // Send final decision email to student
            emailService.sendFinalDecisionNotification(registration, "APPROVED", feedback);

            return saved;
        }
        return null;
    }

    // Admin final rejection
    public PhDRegistration adminReject(Long id, String feedback) {
        PhDRegistration registration = getRegistrationById(id);
        if (registration != null && registration.getStatus().equals(PhDRegistration.STATUS_DIRECTOR_APPROVED)) {
            registration.setStatus(PhDRegistration.STATUS_ADMIN_REJECTED);
            registration.setAdminFeedback(feedback);
            registration.setAdminApprovalDate(LocalDateTime.now());

            PhDRegistration saved = registrationRepository.save(registration);

            // Send final decision email to student
            emailService.sendFinalDecisionNotification(registration, "REJECTED", feedback);

            return saved;
        }
        return null;
    }

    // Get registrations by status (for admin/director filtering)
    public List<PhDRegistration> getRegistrationsByStatus(String status) {
        return registrationRepository.findByStatus(status);
    }
    public List<PhDRegistration> getAllRegistrations() {
        return registrationRepository.findAll();
    }
    public List<PhDRegistration> getDirectorApprovedRegistrations() {
        return registrationRepository.findByStatus(PhDRegistration.STATUS_DIRECTOR_APPROVED);
    }
}
