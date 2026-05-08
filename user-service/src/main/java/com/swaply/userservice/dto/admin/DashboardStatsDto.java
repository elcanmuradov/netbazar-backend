package com.swaply.userservice.dto.admin;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardStatsDto {
    private long totalProducts;
    private long totalUsers;
    private long totalSellers;
    private long totalReports;
    private long activeReports;
    private double productGrowth;
    private double userGrowth;
}
