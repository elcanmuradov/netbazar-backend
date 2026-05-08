package com.swaply.productservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "notification-service", url = "http://notification-service:8080")
@Component
public interface NotificationClient {

    @PostMapping("/user/product-deleted")
    void sendProductDeletedEmail(@RequestBody ProductDeletedEmailRequest request);

    record ProductDeletedEmailRequest(String email, String productTitle, String reason) {}
}
