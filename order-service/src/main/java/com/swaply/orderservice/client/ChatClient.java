package com.swaply.orderservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;

@FeignClient(name = "chat-service", url = "http://chat-service:8080")
@Component
public interface ChatClient {

    @PostMapping("/chat/system/order-placed")
    void sendOrderPlacedNotice(@RequestBody OrderNoticeRequest request);

    record OrderNoticeRequest(UUID sellerId, String buyerName, String productTitle) {}
}

