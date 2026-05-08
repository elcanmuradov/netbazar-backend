package com.swaply.chatservice.dto;

import com.swaply.chatservice.utils.enums.MessageStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TicketResponseDto {
    private String id;

    private String userName;

    private String userEmail;

    private String title;

    private String userReport;

    private String adminResponse;

    private LocalDateTime responseTime;

    private LocalDateTime reportTime;

    @Builder.Default
    private MessageStatus status = MessageStatus.PENDING;

}
