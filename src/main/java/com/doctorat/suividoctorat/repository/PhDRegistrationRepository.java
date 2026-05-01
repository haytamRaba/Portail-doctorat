package com.doctorat.suividoctorat.repository;

import com.doctorat.suividoctorat.entity.PhDRegistration;
import com.doctorat.suividoctorat.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PhDRegistrationRepository extends JpaRepository<PhDRegistration, Long> {

    // Find all registrations for a specific doctorant
    List<PhDRegistration> findByDoctorant(User doctorant);

    // Find all registrations by status
    List<PhDRegistration> findByStatus(String status);

    // Find registrations by director name
    List<PhDRegistration> findByDirectorName(String directorName);
}