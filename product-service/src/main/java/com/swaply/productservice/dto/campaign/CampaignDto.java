package com.swaply.productservice.dto.campaign;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignDto {
    private UUID id;
    private String name;
    private String description;
    private String discountType;        // PERCENTAGE or FIXED_AMOUNT
    private BigDecimal discountValue;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean isActive;
    private String bannerUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
