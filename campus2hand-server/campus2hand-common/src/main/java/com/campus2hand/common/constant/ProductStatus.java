package com.campus2hand.common.constant;

import lombok.Getter;

@Getter
public enum ProductStatus {
    DRAFT("draft", "草稿", 0),
    PENDING_AUDIT("pending_audit", "待审核", 1),
    AUDIT_PASSED("audit_passed", "审核通过", 2),
    AUDIT_REJECTED("audit_rejected", "审核拒绝", 3),
    ON_SALE("on_sale", "在售", 4),
    DELISTED("delisted", "已下架", 5),
    SOLD("sold", "已售出", 6);

    private final String code;
    private final String description;
    private final int sequence;

    ProductStatus(String code, String description, int sequence) {
        this.code = code;
        this.description = description;
        this.sequence = sequence;
    }

    public static ProductStatus fromCode(String code) {
        for (ProductStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown product status: " + code);
    }

    public boolean isVisible() {
        return this == ON_SALE;
    }

    public boolean canBuy() {
        return this == ON_SALE;
    }

    public boolean canUpdate() {
        return this == DRAFT || this == AUDIT_REJECTED;
    }
}