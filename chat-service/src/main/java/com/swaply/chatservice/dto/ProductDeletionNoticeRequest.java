package com.swaply.chatservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDeletionNoticeRequest {
    private UUID receiverId;
    private String productTitle;
    private String reason;
}
