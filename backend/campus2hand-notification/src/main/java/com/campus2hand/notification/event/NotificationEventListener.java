package com.campus2hand.notification.event;

import com.campus2hand.notification.entity.Notification;
import com.campus2hand.notification.mapper.NotificationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationMapper notificationMapper;

    @Async
    @EventListener
    public void handleOrderStatusEvent(OrderStatusEvent event) {
        try {
            createOrderStatusNotification(event.getUserId(), event);
            if (event.getRelatedUserId() != null && !event.getRelatedUserId().equals(event.getUserId())) {
                createOrderStatusNotification(event.getRelatedUserId(), event);
            }
            log.info("Notification created for order status change: orderNo={}, status={}", 
                    event.getOrderNo(), event.getNewStatus());
        } catch (Exception e) {
            log.error("Failed to create notification for order status change: orderNo={}", 
                    event.getOrderNo(), e);
        }
    }

    private void createOrderStatusNotification(Long userId, OrderStatusEvent event) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType("order_status");
        notification.setOrderId(event.getOrderId());
        notification.setProductId(event.getProductId());
        notification.setTitle(buildNotificationTitle(event));
        notification.setContent(buildNotificationContent(event));
        notification.setIsRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        notificationMapper.insert(notification);
    }

    private String buildNotificationTitle(OrderStatusEvent event) {
        return switch (event.getNewStatus()) {
            case "paid" -> "订单已支付";
            case "shipped" -> "卖家已发货";
            case "completed" -> "订单已完成";
            case "cancelled" -> "订单已取消";
            case "refunding" -> "退款申请已提交";
            case "refunded" -> "退款已完成";
            default -> "订单状态变更";
        };
    }

    private String buildNotificationContent(OrderStatusEvent event) {
        String productInfo = event.getProductName() != null ? event.getProductName() : "商品";
        return switch (event.getNewStatus()) {
            case "paid" -> String.format("您的订单 %s 已支付成功，等待卖家发货", event.getOrderNo());
            case "shipped" -> String.format("卖家已发货，商品「%s」正在配送中", productInfo);
            case "completed" -> String.format("订单 %s 已完成，感谢您的使用", event.getOrderNo());
            case "cancelled" -> String.format("订单 %s 已取消", event.getOrderNo());
            case "refunding" -> String.format("订单 %s 退款申请已提交", event.getOrderNo());
            case "refunded" -> String.format("订单 %s 退款已完成", event.getOrderNo());
            default -> String.format("订单 %s 状态变更为：%s", event.getOrderNo(), event.getNewStatus());
        };
    }
}