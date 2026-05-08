package com.swaply.orderservice.dto.discount;

import com.swaply.orderservice.utils.enums.DiscountType;
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
public class DiscountRuleDto {
    private UUID id;
    private String code;
    private DiscountType type;
    private BigDecimal value;
    private BigDecimal minOrderAmount;
    private BigDecimal maxDiscountAmount;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer usageLimit;
    private Integer usageCount;
    private UUID sellerId;
    private UUID productId;
    private Integer minQuantity; // volume discount threshold
    private Boolean isActive;
}

