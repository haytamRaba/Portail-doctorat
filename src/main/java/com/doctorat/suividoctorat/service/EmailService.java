package com.doctorat.suividoctorat.service;

import com.doctorat.suividoctorat.entity.PhDRegistration;
import com.doctorat.suividoctorat.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    // Send email to director when student submits application
    public void sendDirectorNotification(PhDRegistration registration, User director) {
        String subject = "New PhD Application Needs Your Review";
        String content = String.format(
                "Dear %s,\n\n" +
                        "A new PhD application has been submitted by %s.\n\n" +
                        "Thesis Subject: %s\n" +
                        "Research Domain: %s\n\n" +
                        "Please review the application at: http://localhost:8080/registration/%d\n\n" +
                        "Best regards,\n" +
                        "PhD Tracking System",
                director.getFullName(),
                registration.getDoctorant().getFullName(),
                registration.getThesisSubject(),
                registration.getResearchDomain(),
                registration.getId()
        );

        sendEmail(director.getEmail(), subject, content);
    }

    // Send email to student when director approves/rejects
    public void sendDirectorDecisionNotification(PhDRegistration registration, String decision, String comments) {
        String subject = "Director Decision on Your PhD Application";
        String content = String.format(
                "Dear %s,\n\n" +
                        "Your PhD application has been reviewed by your director.\n\n" +
                        "Decision: %s\n" +
                        "Comments: %s\n\n",
                registration.getDoctorant().getFullName(),
                decision,
                comments != null ? comments : "No comments provided"
        );

        if (decision.equals("APPROVED")) {
            content += "Your application has been forwarded to the administration for final approval.\n\n";
        } else {
            content += "Please contact your director for more information.\n\n";
        }

        content += "Login to view details: http://localhost:8080/dashboard\n\n" +
                "Best regards,\nPhD Tracking System";

        sendEmail(registration.getDoctorant().getEmail(), subject, content);
    }

    // Send email to admin when director approves
    public void sendAdminNotification(PhDRegistration registration, User admin) {
        String subject = "PhD Application Ready for Admin Approval";
        String content = String.format(
                "Dear Admin,\n\n" +
                        "A PhD application has been approved by the director and needs your final approval.\n\n" +
                        "Student: %s\n" +
                        "Thesis Subject: %s\n" +
                        "Director: %s\n\n" +
                        "Review at: http://localhost:8080/registration/%d\n\n" +
                        "Best regards,\n" +
                        "PhD Tracking System",
                registration.getDoctorant().getFullName(),
                registration.getThesisSubject(),
                registration.getDirectorName(),
                registration.getId()
        );

        sendEmail(admin.getEmail(), subject, content);
    }

    // Send email to student for final decision
    public void sendFinalDecisionNotification(PhDRegistration registration, String decision, String comments) {
        String subject = "Final Decision on Your PhD Application";
        String content = String.format(
                "Dear %s,\n\n" +
                        "The administration has made a final decision on your PhD application.\n\n" +
                        "Final Decision: %s\n" +
                        "Comments: %s\n\n",
                registration.getDoctorant().getFullName(),
                decision,
                comments != null ? comments : "No comments provided"
        );

        if (decision.equals("APPROVED")) {
            content += "🎉 CONGRATULATIONS! Your PhD application has been APPROVED!\n\n" +
                    "You are now officially enrolled in the PhD program.\n\n";
        } else {
            content += "Your application has been rejected. Please contact the administration for more information.\n\n";
        }

        content += "Login to view details: http://localhost:8080/dashboard\n\n" +
                "Best regards,\nPhD Tracking System";

        sendEmail(registration.getDoctorant().getEmail(), subject, content);
    }

    // Generic email sender
    private void sendEmail(String to, String subject, String content) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);

            mailSender.send(message);
            System.out.println("Email sent to: " + to);
        } catch (Exception e) {
            System.out.println("Failed to send email to: " + to);
            System.out.println("Error: " + e.getMessage());
            // Print email content to console for testing
            System.out.println("=== EMAIL CONTENT (for testing) ===");
            System.out.println("To: " + to);
            System.out.println("Subject: " + subject);
            System.out.println("Content: " + content);
            System.out.println("====================================");
        }
    }
}