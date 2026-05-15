package com.campus2hand.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private Long id;
    private String orderNo;
    private Long buyerId;
    private Long sellerId;
    private Long productId;
    private String productTitle;
    private String productCoverImage;
    private Long amount;
    private String status;
    private String paymentMethod;
    private String transactionId;
    private Long refundAmount;
    private String refundReason;
    private LocalDateTime refundRequestedAt;
    private LocalDateTime refundCompletedAt;
    private LocalDateTime paidAt;
    private LocalDateTime completedAt;
    private LocalDateTime cancelledAt;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}