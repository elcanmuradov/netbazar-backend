package com.swaply.orderservice.service;

import com.swaply.orderservice.dto.campaign.CampaignDto;
import com.swaply.orderservice.entity.Campaign;
import com.swaply.orderservice.repository.jpa.CampaignRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CampaignService {
    private final CampaignRepository campaignRepository;

    public List<CampaignDto> getActiveCampaigns() {
        return campaignRepository.findAllByIsActiveTrueOrderByStartDateDesc()
                .stream()
                .filter(c -> isCurrentlyActive(c))
                .map(this::toDto)
                .collect(Collectors.toList());
    }


    public List<CampaignDto> getAllCampaigns() {
        return campaignRepository.findAll()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<CampaignDto> getCampaignsByStatus(Boolean active) {
        return campaignRepository.findAll()
                .stream()
                .filter(c -> java.util.Objects.equals(c.getIsActive(), active))
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public CampaignDto createCampaign(CampaignDto dto) {
        Campaign campaign = Campaign.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .discountType(dto.getDiscountType())
                .discountValue(dto.getDiscountValue())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .isActive(true)
                .bannerUrl(dto.getBannerUrl())
                .build();

        Campaign saved = campaignRepository.save(campaign);
        return toDto(saved);
    }

    @Transactional
    public CampaignDto updateCampaign(UUID id, CampaignDto dto) {
        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Kampanya tapılmadı: " + id));

        campaign.setName(dto.getName());
        campaign.setDescription(dto.getDescription());
        campaign.setDiscountType(dto.getDiscountType());
        campaign.setDiscountValue(dto.getDiscountValue());
        campaign.setStartDate(dto.getStartDate());
        campaign.setEndDate(dto.getEndDate());
        campaign.setIsActive(dto.getIsActive());
        campaign.setBannerUrl(dto.getBannerUrl());

        Campaign updated = campaignRepository.save(campaign);
        return toDto(updated);
    }

    @Transactional
    public CampaignDto activateCampaign(UUID id) {
        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Kampanya tapılmadı: " + id));
        campaign.setIsActive(true);
        return toDto(campaignRepository.save(campaign));
    }


    @Transactional
    public CampaignDto deactivateCampaign(UUID id) {
        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Kampanya tapılmadı: " + id));
        campaign.setIsActive(false);
        return toDto(campaignRepository.save(campaign));
    }

    @Transactional
    public void deleteCampaign(UUID id) {
        campaignRepository.deleteById(id);
    }

    private boolean isCurrentlyActive(Campaign campaign) {
        if (campaign == null || !Boolean.TRUE.equals(campaign.getIsActive())) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();

        if (campaign.getStartDate() != null && now.isBefore(campaign.getStartDate())) {
            return false; // Campaign hasn't started yet
        }

        if (campaign.getEndDate() != null && now.isAfter(campaign.getEndDate())) {
            return false; // Campaign has ended
        }

        return true;
    }

    private CampaignDto toDto(Campaign campaign) {
        return CampaignDto.builder()
                .id(campaign.getId())
                .name(campaign.getName())
                .description(campaign.getDescription())
                .discountType(campaign.getDiscountType())
                .discountValue(campaign.getDiscountValue())
                .startDate(campaign.getStartDate())
                .endDate(campaign.getEndDate())
                .isActive(campaign.getIsActive())
                .bannerUrl(campaign.getBannerUrl())
                .createdAt(campaign.getCreatedAt())
                .updatedAt(campaign.getUpdatedAt())
                .build();
    }
}

