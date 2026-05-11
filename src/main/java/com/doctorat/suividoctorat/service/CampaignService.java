package com.doctorat.suividoctorat.service;

import com.doctorat.suividoctorat.entity.Campaign;
import com.doctorat.suividoctorat.repository.CampaignRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CampaignService {

    @Autowired
    private CampaignRepository campaignRepository;

    public Campaign createCampaign(Campaign campaign) {
        // Auto-update status based on dates
        updateCampaignStatus(campaign);
        campaign.setCreatedAt(LocalDateTime.now());
        campaign.setUpdatedAt(LocalDateTime.now());
        return campaignRepository.save(campaign);
    }

    // Update existing campaign
    public Campaign updateCampaign(Long id, Campaign campaignDetails) {
        Campaign campaign = campaignRepository.findById(id).orElse(null);
        if (campaign != null) {
            campaign.setName(campaignDetails.getName());
            campaign.setDescription(campaignDetails.getDescription());
            campaign.setStartDate(campaignDetails.getStartDate());
            campaign.setEndDate(campaignDetails.getEndDate());
            campaign.setMaxApplications(campaignDetails.getMaxApplications());
            campaign.setStatus(campaignDetails.getStatus());
            campaign.setUpdatedAt(LocalDateTime.now());
            updateCampaignStatus(campaign);
            return campaignRepository.save(campaign);
        }
        return null;
    }

    // Get all campaigns
    public List<Campaign> getAllCampaigns() {
        return campaignRepository.findAll();
    }

    // Get active campaigns
    public List<Campaign> getActiveCampaigns() {
        return campaignRepository.findActiveCampaigns();
    }

    // Get campaign by ID
    public Campaign getCampaignById(Long id) {
        return campaignRepository.findById(id).orElse(null);
    }

    // Check if applications can be submitted
    public boolean canSubmitApplications() {
        return campaignRepository.hasActiveCampaign();
    }

    // Get current active campaign for doctorant view
    public Campaign getCurrentActiveCampaign() {
        List<Campaign> activeCampaigns = campaignRepository.getCurrentActiveCampaigns();
        return activeCampaigns.isEmpty() ? null : activeCampaigns.get(0);
    }

    // Delete campaign
    public void deleteCampaign(Long id) {
        campaignRepository.deleteById(id);
    }

    // Update campaign status based on dates
    private void updateCampaignStatus(Campaign campaign) {
        LocalDateTime now = LocalDateTime.now();

        if (campaign.getStartDate().isAfter(now)) {
            campaign.setStatus("UPCOMING");
        } else if (campaign.getEndDate().isBefore(now)) {
            campaign.setStatus("CLOSED");
        } else if ("ACTIVE".equals(campaign.getStatus()) ||
                (campaign.getStartDate().isBefore(now) && campaign.getEndDate().isAfter(now))) {
            campaign.setStatus("ACTIVE");
        }
    }

    // Manually activate/deactivate campaign
    public Campaign setCampaignStatus(Long id, String status) {
        Campaign campaign = campaignRepository.findById(id).orElse(null);
        if (campaign != null) {
            campaign.setStatus(status);
            campaign.setUpdatedAt(LocalDateTime.now());
            return campaignRepository.save(campaign);
        }
        return null;
    }
}
