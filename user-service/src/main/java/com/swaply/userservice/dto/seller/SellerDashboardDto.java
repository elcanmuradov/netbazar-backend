package com.swaply.userservice.dto.seller;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SellerDashboardDto {
    private SellerStatsDto stats;
    private List<SellerOrderDto> orders;
    private List<String> paymentMethods;
}
