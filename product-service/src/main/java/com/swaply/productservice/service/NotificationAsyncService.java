package com.swaply.productservice.service;

import com.swaply.productservice.client.NotificationClient;
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
    public void sendProductDeletedEmailAsync(NotificationClient.ProductDeletedEmailRequest request) {
        try {
            log.info("Sending product deleted email asynchronously to {}", request.email());
            notificationClient.sendProductDeletedEmail(request);
        } catch (Exception e) {
            log.error("Error sending product deleted email to {}: {}", request.email(), e.getMessage());
        }
    }
}
