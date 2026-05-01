package com.doctorat.suividoctorat.service;

import com.doctorat.suividoctorat.entity.PhDRegistration;
import com.doctorat.suividoctorat.entity.User;
import com.doctorat.suividoctorat.repository.PhDRegistrationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class PhDRegistrationService {

    @Autowired
    private PhDRegistrationRepository registrationRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    // Save PhD registration with files
    public PhDRegistration saveRegistration(PhDRegistration registration,
                                            MultipartFile diplomaFile,
                                            MultipartFile cvFile,
                                            MultipartFile additionalFile) throws IOException {

        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Save files with unique names
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
        registration.setStatus("PENDING");

        return registrationRepository.save(registration);
    }

    // Helper method to save file
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

    // Get all registrations for a user
    public List<PhDRegistration> getRegistrationsByDoctorant(User doctorant) {
        return registrationRepository.findByDoctorant(doctorant);
    }

    // Get registration by ID
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
        return registrationRepository.findByStatus("PENDING");
    }

    public List<PhDRegistration> getRegistrationsByDirector(String directorName) {
        return registrationRepository.findByDirectorName(directorName);
    }
}