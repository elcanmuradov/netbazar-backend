package com.swaply.orderservice.controller;

import com.swaply.orderservice.dto.ApiResponse;
import com.swaply.orderservice.dto.campaign.CampaignDto;
import com.swaply.orderservice.service.CampaignService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/campaigns")
@RequiredArgsConstructor
public class CampaignController {
    private final CampaignService campaignService;


    @GetMapping
    public ResponseEntity<ApiResponse<List<CampaignDto>>> getCampaigns() {
        return ResponseEntity.ok(ApiResponse.success(campaignService.getActiveCampaigns()));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<CampaignDto>>> getActiveCampaigns() {
        return ResponseEntity.ok(ApiResponse.success(campaignService.getActiveCampaigns()));
    }
}

