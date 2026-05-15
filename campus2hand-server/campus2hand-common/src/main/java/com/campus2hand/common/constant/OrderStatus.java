package com.campus2hand.common.constant;

import lombok.Getter;

@Getter
public enum OrderStatus {
    PENDING("pending", "待支付", 0),
    PAID("paid", "已支付", 1),
    SHIPPED("shipped", "已发货", 2),
    COMPLETED("completed", "已完成", 3),
    CANCELLED("cancelled", "已取消", 4),
    REFUNDING("refunding", "退款中", 5),
    REFUNDED("refunded", "已退款", 6);

    private final String code;
    private final String description;
    private final int sequence;

    OrderStatus(String code, String description, int sequence) {
        this.code = code;
        this.description = description;
        this.sequence = sequence;
    }

    public static OrderStatus fromCode(String code) {
        for (OrderStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown order status: " + code);
    }

    public boolean canCancel() {
        return this == PENDING;
    }

    public boolean canRefund() {
        return this == PAID || this == SHIPPED;
    }

    public boolean isTerminal() {
        return this == COMPLETED || this == CANCELLED || this == REFUNDED;
    }
}