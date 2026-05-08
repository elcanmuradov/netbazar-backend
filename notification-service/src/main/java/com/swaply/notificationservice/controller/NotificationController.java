package com.swaply.notificationservice.controller;

import com.swaply.notificationservice.dto.ProductDeletedEmailRequest;
import com.swaply.notificationservice.dto.TicketDto;
import com.swaply.notificationservice.dto.VerificationRequest;
import com.swaply.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;


    @PostMapping("/user/send")
    public void sendNotification(@RequestBody VerificationRequest request) {
        notificationService.sendVerificationEmail(request);
    }

    @PostMapping("/user/product-deleted")
    public void sendProductDeletedEmail(@RequestBody ProductDeletedEmailRequest request) {
        notificationService.sendProductDeletedEmail(request);
    }
    @PostMapping("/user/send-ticket-response")
    public void sendTicketResponse(@RequestBody TicketDto ticket){
        notificationService.sendTicketResponse(ticket);
    }

}
