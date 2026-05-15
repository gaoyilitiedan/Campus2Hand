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
public class SettlementRetryJob {

    private final OrderMapper orderMapper;

    @Scheduled(fixedRate = 600000)
    public void retryFailedSettlements() {
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        
        List<Order> failedSettlements = orderMapper.selectList(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getStatus, "settlement_failed")
                        .lt(Order::getUpdatedAt, oneHourAgo)
        );

        for (Order order : failedSettlements) {
            try {
                boolean success = retrySettlement(order.getId());
                if (success) {
                    order.setStatus("settled");
                    order.setUpdatedAt(LocalDateTime.now());
                    orderMapper.updateById(order);
                    log.info("Settlement retry successful: orderNo={}", order.getOrderNo());
                } else {
                    order.setSettlementRetryCount(order.getSettlementRetryCount() + 1);
                    orderMapper.updateById(order);
                    log.warn("Settlement retry failed: orderNo={}, retryCount={}", 
                            order.getOrderNo(), order.getSettlementRetryCount());
                }
            } catch (Exception e) {
                log.error("Failed to retry settlement for order: {}", order.getOrderNo(), e);
            }
        }
    }

    private boolean retrySettlement(Long orderId) {
        return false;
    }
}