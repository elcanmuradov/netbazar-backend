package com.swaply.chatservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderNoticeRequest {
    private UUID sellerId;
    private String buyerName;
    private String productTitle;
}
