package com.swaply.userservice.client;

import com.swaply.userservice.dto.user.create.VerificationRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;


@FeignClient(name = "notification-service", url = "http://notification-service:8080")
@Component
public interface NotificationClient {
    @PostMapping("/user/send")
    void sendVerificationCode(VerificationRequest request);
}
