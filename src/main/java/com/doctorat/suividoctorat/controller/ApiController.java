package com.doctorat.suividoctorat.controller;

import com.doctorat.suividoctorat.entity.PhDRegistration;
import com.doctorat.suividoctorat.entity.User;
import com.doctorat.suividoctorat.service.PhDRegistrationService;
import com.doctorat.suividoctorat.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class ApiController {

    @Autowired
    private PhDRegistrationService phdRegistrationService;

    @Autowired
    private UserService userService;


    private User getCurrentUser(HttpSession session) {
        Long userId = (Long) session.getAttribute("loggedUserId");
        if (userId == null) {
            return null;
        }
        return userService.findUserById(userId);
    }

    // Get statistics for dashboard
    @GetMapping("/stats")
    public Map<String, Object> getStats(HttpSession session) {
        User currentUser = getCurrentUser(session);

        Map<String, Object> stats = new HashMap<>();

        if (currentUser == null) {
            stats.put("error", "Not logged in");
            return stats;
        }

        List<PhDRegistration> allRegistrations = phdRegistrationService.getAllRegistrations();

        // Count by status
        long pendingCount = allRegistrations.stream()
                .filter(r -> r.getStatus().equals("PENDING"))
                .count();

        long directorApprovedCount = allRegistrations.stream()
                .filter(r -> r.getStatus().equals("DIRECTOR_APPROVED"))
                .count();

        long approvedCount = allRegistrations.stream()
                .filter(r -> r.getStatus().equals("APPROVED"))
                .count();

        long rejectedCount = allRegistrations.stream()
                .filter(r -> r.getStatus().equals("REJECTED") || r.getStatus().equals("DIRECTOR_REJECTED"))
                .count();

        stats.put("total", allRegistrations.size());
        stats.put("pending", pendingCount);
        stats.put("directorApproved", directorApprovedCount);
        stats.put("approved", approvedCount);
        stats.put("rejected", rejectedCount);
        stats.put("userRole", currentUser.getRole());
        stats.put("userName", currentUser.getFullName());

        return stats;
    }

    // Get recent applications
    @GetMapping("/recent-applications")
    public List<Map<String, Object>> getRecentApplications(HttpSession session) {
        User currentUser = getCurrentUser(session);

        if (currentUser == null) {
            return List.of();
        }

        List<PhDRegistration> registrations = phdRegistrationService.getAllRegistrations();

        // Get last 10 applications, newest first
        return registrations.stream()
                .sorted((a, b) -> b.getSubmissionDate().compareTo(a.getSubmissionDate()))
                .limit(10)
                .map(reg -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", reg.getId());
                    map.put("studentName", reg.getDoctorant().getFullName());
                    map.put("thesisSubject", reg.getThesisSubject());
                    map.put("directorName", reg.getDirectorName());
                    map.put("status", reg.getStatus());
                    map.put("submissionDate", reg.getSubmissionDate().toString());
                    return map;
                })
                .collect(Collectors.toList());
    }

    // Get applications by status (for filtering)
    @GetMapping("/applications/status/{status}")
    public List<Map<String, Object>> getApplicationsByStatus(@PathVariable String status, HttpSession session) {
        User currentUser = getCurrentUser(session);

        if (currentUser == null) {
            return List.of();
        }

        List<PhDRegistration> registrations = phdRegistrationService.getRegistrationsByStatus(status);

        return registrations.stream()
                .map(reg -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", reg.getId());
                    map.put("studentName", reg.getDoctorant().getFullName());
                    map.put("thesisSubject", reg.getThesisSubject());
                    map.put("directorName", reg.getDirectorName());
                    map.put("status", reg.getStatus());
                    map.put("submissionDate", reg.getSubmissionDate().toString());
                    return map;
                })
                .collect(Collectors.toList());
    }
}