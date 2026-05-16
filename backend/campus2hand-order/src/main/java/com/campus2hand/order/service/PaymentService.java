package com.campus2hand.order.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus2hand.common.constant.ErrorCode;
import com.campus2hand.common.exception.BusinessException;
import com.campus2hand.order.entity.Order;
import com.campus2hand.order.mapper.OrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final OrderMapper orderMapper;

    @Value("${app.payment.timeout-minutes:30}")
    private int paymentTimeoutMinutes;

    @Value("${app.payment.mock-enabled:true}")
    private boolean mockEnabled;

    public String createPayment(Long orderId, Long userId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND, "订单不存在");
        }
        if (!order.getBuyerId().equals(userId)) {
            throw new BusinessException(ErrorCode.NO_PERMISSION, "无权操作此订单");
        }
        if (!"pending_payment".equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.PAYMENT_FAILED, "订单状态不允许支付");
        }

        String transactionId = "TXN" + System.currentTimeMillis() + orderId;

        if (mockEnabled) {
            log.info("Mock payment created: orderId={}, transactionId={}, amount={}",
                    orderId, transactionId, order.getAmount());
            return transactionId;
        }

        log.info("Payment creation request: orderId={}, transactionId={}", orderId, transactionId);
        return transactionId;
    }

    @Transactional
    public void handlePaymentCallback(String transactionId, boolean success, String paymentMethod) {
        Order order = orderMapper.selectOne(
                new LambdaQueryWrapper<Order>().eq(Order::getTransactionId, transactionId)
        );
        if (order == null) {
            log.warn("Payment callback for unknown transaction: {}", transactionId);
            return;
        }
        if (!"pending_payment".equals(order.getStatus())) {
            log.warn("Payment callback for non-pending order: orderId={}, status={}",
                    order.getId(), order.getStatus());
            return;
        }

        if (success) {
            order.setStatus("paid");
            order.setPaymentMethod(paymentMethod);
            order.setTransactionId(transactionId);
            order.setPaidAt(LocalDateTime.now());
            log.info("Payment confirmed: orderId={}, transactionId={}", order.getId(), transactionId);
        } else {
            order.setStatus("payment_failed");
            log.warn("Payment failed: orderId={}, transactionId={}", order.getId(), transactionId);
        }
        orderMapper.updateById(order);
    }

    @Transactional
    public void processRefund(Long orderId, Long refundAmount, String reason) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND, "订单不存在");
        }
        if (!"refund_requested".equals(order.getStatus()) && !"disputed".equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.REFUND_FAILED, "订单状态不允许退款");
        }

        String refundId = "RFN" + System.currentTimeMillis() + orderId;

        if (mockEnabled) {
            order.setStatus("refunded");
            order.setRefundId(refundId);
            order.setRefundAmount(refundAmount);
            order.setRefundReason(reason);
            order.setRefundCompletedAt(LocalDateTime.now());
            orderMapper.updateById(order);
            log.info("Mock refund processed: orderId={}, refundId={}, amount={}",
                    orderId, refundId, refundAmount);
            return;
        }

        order.setRefundId(refundId);
        order.setRefundAmount(refundAmount);
        order.setRefundReason(reason);
        orderMapper.updateById(order);
        log.info("Refund request submitted: orderId={}, refundId={}", orderId, refundId);
    }

    public boolean verifyPayment(String transactionId) {
        if (mockEnabled) {
            return true;
        }
        Order order = orderMapper.selectOne(
                new LambdaQueryWrapper<Order>().eq(Order::getTransactionId, transactionId)
        );
        return order != null && "paid".equals(order.getStatus());
    }

    @Transactional
    public void cancelExpiredPayment(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !"pending_payment".equals(order.getStatus())) {
            return;
        }
        if (order.getCreatedAt().plusMinutes(paymentTimeoutMinutes).isBefore(LocalDateTime.now())) {
            order.setStatus("cancelled");
            order.setCancelledAt(LocalDateTime.now());
            orderMapper.updateById(order);
            log.info("Payment expired, order cancelled: orderId={}", orderId);
        }
    }
}