package com.swaply.userservice.service.seller;

import com.swaply.userservice.client.ProductClient;
import com.swaply.userservice.dto.ApiResponse;
import com.swaply.userservice.dto.seller.SellerDashboardDto;
import com.swaply.userservice.dto.seller.SellerOrderDto;
import com.swaply.userservice.dto.seller.SellerStatsDto;
import com.swaply.userservice.entity.Seller;
import com.swaply.userservice.exception.AuthException;
import com.swaply.userservice.repository.SellerRepository;
import com.swaply.userservice.service.MediaStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SellerService {
    private final SellerRepository sellerRepository;
    private final ProductClient productClient;
    private final MediaStorageService mediaStorageService;

    public SellerDashboardDto getSellerDashboard(UUID sellerId) {
        log.info("Fetching dashboard for seller: {}", sellerId);
        
        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new AuthException("Satıcı tapılmadı"));

        Long activeProducts = 0L;
        BigDecimal revenue = BigDecimal.ZERO;
        Long dailyOrders = 0L;
        Long ordersInProgress = 0L;

        try {
            // Try to fetch real seller stats from product-service
            ApiResponse<java.util.Map<String, Object>> statsResponse = (ApiResponse<java.util.Map<String, Object>>) productClient.getSellerStats(sellerId);
            if (statsResponse != null && statsResponse.isSuccess() && statsResponse.getData() != null) {
                java.util.Map<String, Object> statsMap = statsResponse.getData();
                activeProducts = ((Number) statsMap.getOrDefault("activeProducts", activeProducts)).longValue();
                dailyOrders = ((Number) statsMap.getOrDefault("dailyOrders", 0)).longValue();
                ordersInProgress = ((Number) statsMap.getOrDefault("ordersInProgress", 0)).longValue();
                
                Object rev = statsMap.get("revenueToday");
                if (rev instanceof Number) {
                    revenue = new BigDecimal(rev.toString());
                }
                log.info("Successfully fetched seller stats from product-service");
            }
        } catch (Exception e) {
            log.warn("Could not fetch seller stats from product-service, using fallback: {}", e.getMessage());
        }

        List<SellerOrderDto> orderDtos = new java.util.ArrayList<>();
        try {
            // Try to fetch seller's orders
            ApiResponse<List<java.util.Map<String, Object>>> ordersResponse = (ApiResponse<List<java.util.Map<String, Object>>>) productClient.getSellerOrders(sellerId);
            if (ordersResponse != null && ordersResponse.isSuccess() && ordersResponse.getData() != null) {
                List<java.util.Map<String, Object>> ordersList = ordersResponse.getData();
                for (java.util.Map<String, Object> o : ordersList) {
                    orderDtos.add(SellerOrderDto.builder()
                            .id(o.getOrDefault("id", "").toString())
                            .customer(o.getOrDefault("buyerId", "Anonim").toString())
                            .status(o.getOrDefault("status", "RECEIVED").toString())
                            .payment(o.getOrDefault("paymentMethod", "Kartla").toString())
                            .delivery(o.getOrDefault("deliveryCity", "Bakı").toString())
                            .createdAt(o.getOrDefault("createdAt", "").toString())
                            .build());
                }
                log.info("Successfully mapped {} orders for seller", orderDtos.size());
            }
        } catch (Exception e) {
            log.warn("Could not fetch seller orders: {}", e.getMessage());
        }

        SellerStatsDto stats = SellerStatsDto.builder()
                .activeProducts(activeProducts)
                .dailyOrders(dailyOrders)
                .ordersInProgress(ordersInProgress)
                .revenueToday(revenue)
                .build();

        List<String> paymentMethods = Arrays.asList("Nəğd", "Kartla");

        return SellerDashboardDto.builder()
                .stats(stats)
                .orders(orderDtos)
                .paymentMethods(paymentMethods)
                .build();
    }

    public void updateOrderStatus(UUID orderId, String status) {
        log.info("Updating status for order {} to {}", orderId, status);
        productClient.updateOrderStatus(orderId, status);
    }

    public void changeBanner(MultipartFile file, Authentication authentication) {
        try {
            if (authentication == null || authentication.getName() == null) {
                throw new AuthException("Kimlik doğrulaması gereklidir");
            }
            if (file == null || file.isEmpty()) {
                throw new AuthException("Fayl boşdur");
            }
            byte[] fileBytes = file.getBytes();
            String originalFilename = file.getOriginalFilename();
            // Synchronously upload + persist real media URL on the seller
            mediaStorageService.uploadBannerPhoto(fileBytes, originalFilename, authentication.getName());
            log.info("Banner photo uploaded for seller: {}", authentication.getName());
        } catch (AuthException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error uploading banner photo", e);
            throw new AuthException("Banner fotoğrafı yüklenirken hata oluştu");
        }
    }

    public void changeProfilePhoto(MultipartFile file, Authentication authentication) {
        try {
            if (authentication == null || authentication.getName() == null) {
                throw new AuthException("Kimlik doğrulaması gereklidir");
            }
            if (file == null || file.isEmpty()) {
                throw new AuthException("Fayl boşdur");
            }
            byte[] fileBytes = file.getBytes();
            String originalFilename = file.getOriginalFilename();
            // Synchronously upload + persist real media URL on the seller
            mediaStorageService.uploadProfilePhotoForSeller(fileBytes, originalFilename, authentication.getName());
            log.info("Profile photo uploaded for seller: {}", authentication.getName());
        } catch (AuthException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error uploading profile photo", e);
            throw new AuthException("Profil fotoğrafı yüklenirken hata oluştu");
        }
    }

    public Seller getSellerProfile(UUID sellerId) {
        log.info("Fetching profile for seller: {}", sellerId);
        return sellerRepository.findById(sellerId)
                .orElseThrow(() -> new AuthException("Satıcı tapılmadı"));
    }

    public Seller updateSellerProfile(UUID sellerId, Seller updatedSeller) {
        log.info("Updating profile for seller: {}", sellerId);
        
        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new AuthException("Satıcı tapılmadı"));
        
        if (updatedSeller.getName() != null && !updatedSeller.getName().isEmpty()) {
            seller.setName(updatedSeller.getName());
        }
        
        if (updatedSeller.getProfileImageUrl() != null) {
            seller.setProfileImageUrl(updatedSeller.getProfileImageUrl());
        }
        
        if (updatedSeller.getBannerImageUrl() != null) {
            seller.setBannerImageUrl(updatedSeller.getBannerImageUrl());
        }
        
        return sellerRepository.save(seller);
    }

    public List<com.swaply.userservice.dto.seller.SellerSearchDto> searchSellersByUsernamePattern(String pattern) {
        log.info("Searching sellers by username pattern: {}", pattern);
        if (pattern == null || pattern.trim().isEmpty()) {
            return List.of();
        }
        return sellerRepository.searchByUsernamePattern(pattern.trim()).stream()
                .map(seller -> com.swaply.userservice.dto.seller.SellerSearchDto.builder()
                        .id(seller.getId())
                        .username(seller.getUsername())
                        .build())
                .toList();
    }
}
