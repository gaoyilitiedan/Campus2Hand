package com.campus2hand.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatisticsResponse {
    private long totalUsers;
    private long activeUsers;
    private long totalProducts;
    private long onSaleProducts;
    private long totalOrders;
    private long completedOrders;
    private long pendingDisputes;
    private long totalRevenue;
}