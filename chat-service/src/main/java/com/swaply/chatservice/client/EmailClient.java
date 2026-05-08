package com.swaply.chatservice.client;

import com.swaply.chatservice.dto.TicketResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "notification-service", url = "http://notification-service:8080")
@Component
public interface EmailClient {
    @PostMapping("/user/send-ticket-response")
    void sendTicketResponse(TicketResponseDto ticket);
}
