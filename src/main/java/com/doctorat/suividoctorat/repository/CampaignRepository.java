package com.doctorat.suividoctorat.repository;

import com.doctorat.suividoctorat.entity.Campaign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CampaignRepository extends JpaRepository<Campaign, Long> {

    @Query("SELECT c FROM Campaign c WHERE c.status = 'ACTIVE' AND c.startDate <= CURRENT_TIMESTAMP AND c.endDate >= CURRENT_TIMESTAMP ORDER BY c.endDate ASC")
    List<Campaign> findActiveCampaigns();

    @Query("SELECT c FROM Campaign c WHERE c.startDate > CURRENT_TIMESTAMP")
    List<Campaign> findUpcomingCampaigns();

    List<Campaign> findByStatus(String status);

    @Query("SELECT COUNT(c) > 0 FROM Campaign c WHERE c.status = 'ACTIVE' AND c.startDate <= CURRENT_TIMESTAMP AND c.endDate >= CURRENT_TIMESTAMP")
    boolean hasActiveCampaign();

    @Query("SELECT c FROM Campaign c WHERE c.status = 'ACTIVE' AND c.startDate <= CURRENT_TIMESTAMP AND c.endDate >= CURRENT_TIMESTAMP ORDER BY c.endDate ASC")
    List<Campaign> getCurrentActiveCampaigns();
}
