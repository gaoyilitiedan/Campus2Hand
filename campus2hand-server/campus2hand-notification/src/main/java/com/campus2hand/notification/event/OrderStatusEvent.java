package com.campus2hand.notification.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class OrderStatusEvent extends ApplicationEvent {

    private final Long orderId;
    private final String orderNo;
    private final String previousStatus;
    private final String newStatus;
    private final Long userId;
    private final Long relatedUserId;
    private final Long productId;
    private final String productName;

    public OrderStatusEvent(Object source, Long orderId, String orderNo, String previousStatus,
                            String newStatus, Long userId, Long relatedUserId,
                            Long productId, String productName) {
        super(source);
        this.orderId = orderId;
        this.orderNo = orderNo;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.userId = userId;
        this.relatedUserId = relatedUserId;
        this.productId = productId;
        this.productName = productName;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Object source;
        private Long orderId;
        private String orderNo;
        private String previousStatus;
        private String newStatus;
        private Long userId;
        private Long relatedUserId;
        private Long productId;
        private String productName;

        public Builder source(Object source) {
            this.source = source;
            return this;
        }

        public Builder orderId(Long orderId) {
            this.orderId = orderId;
            return this;
        }

        public Builder orderNo(String orderNo) {
            this.orderNo = orderNo;
            return this;
        }

        public Builder previousStatus(String previousStatus) {
            this.previousStatus = previousStatus;
            return this;
        }

        public Builder newStatus(String newStatus) {
            this.newStatus = newStatus;
            return this;
        }

        public Builder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public Builder relatedUserId(Long relatedUserId) {
            this.relatedUserId = relatedUserId;
            return this;
        }

        public Builder productId(Long productId) {
            this.productId = productId;
            return this;
        }

        public Builder productName(String productName) {
            this.productName = productName;
            return this;
        }

        public OrderStatusEvent build() {
            return new OrderStatusEvent(source, orderId, orderNo, previousStatus,
                    newStatus, userId, relatedUserId, productId, productName);
        }
    }
}