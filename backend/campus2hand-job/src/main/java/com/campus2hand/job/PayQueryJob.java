package com.campus2hand.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus2hand.order.entity.Order;
import com.campus2hand.order.mapper.OrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PayQueryJob {

    private final OrderMapper orderMapper;

    @Scheduled(fixedRate = 30000)
    public void queryPendingPayments() {
        LocalDateTime thirtyMinutesAgo = LocalDateTime.now().minusMinutes(30);
        
        List<Order> pendingOrders = orderMapper.selectList(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getStatus, "pending_payment")
                        .gt(Order::getCreatedAt, thirtyMinutesAgo)
        );

        for (Order order : pendingOrders) {
            try {
                boolean paid = queryWeChatPaymentStatus(order.getOrderNo());
                if (paid) {
                    order.setStatus("paid");
                    order.setPaidAt(LocalDateTime.now());
                    orderMapper.updateById(order);
                    log.info("Payment confirmed via query: orderNo={}", order.getOrderNo());
                }
            } catch (Exception e) {
                log.error("Failed to query payment status for order: {}", order.getOrderNo(), e);
            }
        }
    }

    private boolean queryWeChatPaymentStatus(String orderNo) {
        return false;
    }
}