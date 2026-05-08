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


@Slf4j
@Service
@RequiredArgsConstructor
public class MediaStorageService {
    private final UserRepository userRepository;
    private final SellerRepository sellerRepository;
    private final MediaClient mediaClient;

    @Async
    public void uploadProfilePhotoAsync(MultipartFile file, String userEmail) {
        try {
            log.info("Starting upload profile photo for user: {}", userEmail);
            User user = userRepository.findByEmail(userEmail).orElseThrow(()->new AuthException("User not found with username: " + userEmail));
            var media = mediaClient.upload(file);
            user.setProfileImageUrl(media.getData().get("url"));
            userRepository.save(user);
        }catch (Exception e){
          log.error("Error uploading profile photo for user: {}", userEmail, e);
        }
    }

    @Async
    public void uploadBannerPhoto(MultipartFile file, String sellerEmail) {
        try {
            log.info("Starting upload profile photo for user: {}", sellerEmail);
            Seller seller = sellerRepository.findByEmail(sellerEmail).orElseThrow(()->new AuthException("User not found with username: " + sellerEmail));
            var media = mediaClient.upload(file);
            seller.setBannerImageUrl(media.getData().get("url"));
            sellerRepository.save(seller);
        }catch (Exception e){
            log.error("Error uploading profile photo for user: {}", sellerEmail, e);
        }
    }

    @Async
    public void uploadProfilePhotoForSeller(MultipartFile file, String sellerEmail) {
        try {
            log.info("Starting upload profile photo for user: {}", sellerEmail);
            Seller seller = sellerRepository.findByEmail(sellerEmail).orElseThrow(()->new AuthException("User not found with username: " + sellerEmail));
            var media = mediaClient.upload(file);
            seller.setProfileImageUrl(media.getData().get("url"));
            sellerRepository.save(seller);
        }catch (Exception e){
            log.error("Error uploading profile photo for user: {}", sellerEmail, e);
        }
    }
}
