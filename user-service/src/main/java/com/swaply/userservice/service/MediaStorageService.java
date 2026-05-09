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
import org.springframework.web.multipart.MultipartFile;
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

    @Async
    public void uploadBannerPhoto(byte[] fileBytes, String originalFilename, String sellerEmail) {
        try {
            log.info("Starting upload banner photo for seller: {}", sellerEmail);
            Seller seller = sellerRepository.findByEmail(sellerEmail).orElseThrow(()->new AuthException("User not found with username: " + sellerEmail));
            ByteArrayResource resource = new ByteArrayResource(fileBytes) {
                @Override
                public String getFilename() {
                    return originalFilename;
                }
            };
            var media = mediaClient.uploadBytes(resource);
            seller.setBannerImageUrl(media.getData().get("url"));
            sellerRepository.save(seller);
        }catch (Exception e){
            log.error("Error uploading banner photo for seller: {}", sellerEmail, e);
        }
    }

    @Async
    public void uploadProfilePhotoForSeller(byte[] fileBytes, String originalFilename, String sellerEmail) {
        try {
            log.info("Starting upload profile photo for seller: {}", sellerEmail);
            Seller seller = sellerRepository.findByEmail(sellerEmail).orElseThrow(()->new AuthException("User not found with username: " + sellerEmail));
            ByteArrayResource resource = new ByteArrayResource(fileBytes) {
                @Override
                public String getFilename() {
                    return originalFilename;
                }
            };
            var media = mediaClient.uploadBytes(resource);
            seller.setProfileImageUrl(media.getData().get("url"));
            sellerRepository.save(seller);
        }catch (Exception e){
            log.error("Error uploading profile photo for seller: {}", sellerEmail, e);
        }
    }
}
