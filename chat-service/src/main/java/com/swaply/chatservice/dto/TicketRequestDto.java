package com.swaply.chatservice.dto;

import lombok.Builder;
import lombok.Data;


@Builder
@Data
public class TicketRequestDto {
    private String userId;

    private String title;

    private String userReport;
}
