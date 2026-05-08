package com.swaply.notificationservice.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TicketDto {
    private String id;

    private String userName;

    private String userEmail;

    private String title;

    private String userReport;

    private String adminResponse;

    private LocalDateTime responseTime;

    private LocalDateTime reportTime;

}
