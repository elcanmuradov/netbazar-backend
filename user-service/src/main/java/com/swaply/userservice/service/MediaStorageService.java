package com.swaply.userservice.service;

import com.swaply.userservice.client.MediaClient;
import com.swaply.userservice.entity.Seller;
import com.swaply.userservice.entity.User;
import com.swaply.userservice.exception.AuthException;
import com.swaply.userservice.repository.SellerRepository;
import com.swaply.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.core.io.ByteArrayResource;


@Slf4j
@Service
@RequiredArgsConstructor
public class MediaStorageService {
    private final UserRepository userRepository;
    private final SellerRepository sellerRepository;
    private final MediaClient mediaClient;

    @Async
    public void uploadProfilePhotoAsync(byte[] fileBytes, String originalFilename, String userEmail) {
        try {
            log.info("Starting upload profile photo for user: {}", userEmail);
            User user = userRepository.findByEmail(userEmail).orElseThrow(()->new AuthException("User not found with username: " + userEmail));
            ByteArrayResource resource = new ByteArrayResource(fileBytes) {
                @Override
                public String getFilename() {
                    return originalFilename;
                }
            };
            var media = mediaClient.uploadBytes(resource);
            user.setProfileImageUrl(media.getData().get("url"));
            userRepository.save(user);
        }catch (Exception e){
          log.error("Error uploading profile photo for user: {}", userEmail, e);
        }
    }

    public void uploadBannerPhoto(byte[] fileBytes, String originalFilename, String sellerEmail) {
        log.info("Starting upload banner photo for seller: {}", sellerEmail);
        Seller seller = sellerRepository.findByEmail(sellerEmail)
                .orElseThrow(() -> new AuthException("User not found with username: " + sellerEmail));
        ByteArrayResource resource = new ByteArrayResource(fileBytes) {
            @Override
            public String getFilename() {
                return originalFilename;
            }
        };
        var media = mediaClient.uploadBytes(resource);
        if (media == null || media.getData() == null || media.getData().get("url") == null) {
            log.error("Media service returned no URL for banner upload (seller: {})", sellerEmail);
            throw new AuthException("Media service banner üçün URL qaytarmadı");
        }
        seller.setBannerImageUrl(media.getData().get("url"));
        sellerRepository.save(seller);
        log.info("Banner saved for seller {} -> {}", sellerEmail, media.getData().get("url"));
    }

    public void uploadProfilePhotoForSeller(byte[] fileBytes, String originalFilename, String sellerEmail) {
        log.info("Starting upload profile photo for seller: {}", sellerEmail);
        Seller seller = sellerRepository.findByEmail(sellerEmail)
                .orElseThrow(() -> new AuthException("User not found with username: " + sellerEmail));
        ByteArrayResource resource = new ByteArrayResource(fileBytes) {
            @Override
            public String getFilename() {
                return originalFilename;
            }
        };
        var media = mediaClient.uploadBytes(resource);
        if (media == null || media.getData() == null || media.getData().get("url") == null) {
            log.error("Media service returned no URL for profile photo upload (seller: {})", sellerEmail);
            throw new AuthException("Media service profil fotosu üçün URL qaytarmadı");
        }
        seller.setProfileImageUrl(media.getData().get("url"));
        sellerRepository.save(seller);
        log.info("Profile photo saved for seller {} -> {}", sellerEmail, media.getData().get("url"));
    }
}
