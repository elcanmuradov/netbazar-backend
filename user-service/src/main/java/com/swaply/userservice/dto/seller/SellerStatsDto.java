package com.swaply.userservice.dto.seller;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class SellerStatsDto {
    private Long activeProducts;
    private Long dailyOrders;
    private Long ordersInProgress;
    private BigDecimal revenueToday;
}
