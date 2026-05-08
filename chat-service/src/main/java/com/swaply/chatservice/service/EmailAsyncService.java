package com.swaply.chatservice.service;

import com.swaply.chatservice.client.EmailClient;
import com.swaply.chatservice.dto.TicketResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailAsyncService {
    private final EmailClient emailClient;

    @Async
    public void sendTicketResponseAsync(TicketResponseDto ticket) {
        try {
            log.info("Sending ticket response asynchronously for ticket {}", ticket.getId());
            emailClient.sendTicketResponse(ticket);
        } catch (Exception e) {
            log.error("Error sending ticket response for ticket {}: {}", ticket.getId(), e.getMessage());
        }
    }
}
