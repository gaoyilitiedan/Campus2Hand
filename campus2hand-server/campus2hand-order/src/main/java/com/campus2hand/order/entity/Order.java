package com.campus2hand.order.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("orders")
public class Order {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private Long buyerId;
    private Long sellerId;
    private Long productId;
    private Long amount;
    private String status;
    private String paymentMethod;
    private String transactionId;
    private String refundId;
    private Long refundAmount;
    private String refundReason;
    private LocalDateTime refundRequestedAt;
    private LocalDateTime refundCompletedAt;
    private LocalDateTime paidAt;
    private LocalDateTime completedAt;
    private LocalDateTime cancelledAt;
    private LocalDateTime expiresAt;
    @TableField(exist = false)
    private Integer settlementRetryCount;
    @Version
    private Integer version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}