package com.swaply.userservice.service;

import com.swaply.userservice.client.NotificationClient;
import com.swaply.userservice.dto.user.create.VerificationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationAsyncService {
    private final NotificationClient notificationClient;

    @Async
    public void sendVerificationCodeAsync(VerificationRequest request) {
        try {
            log.info("Sending verification code asynchronously to {}", request.getEmail());
            notificationClient.sendVerificationCode(request);
        } catch (Exception e) {
            log.error("Error sending verification code to {}: {}", request.getEmail(), e.getMessage());
        }
    }
}
