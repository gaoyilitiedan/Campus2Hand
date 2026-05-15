package com.campus2hand.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus2hand.order.entity.Order;
import com.campus2hand.order.mapper.OrderMapper;
import com.campus2hand.product.entity.Product;
import com.campus2hand.product.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class OrderTimeoutJob {

    private final OrderMapper orderMapper;
    private final ProductMapper productMapper;

    @Scheduled(fixedRate = 60000)
    public void cancelExpiredOrders() {
        List<Order> expiredOrders = orderMapper.selectList(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getStatus, "pending_payment")
                        .lt(Order::getExpiresAt, LocalDateTime.now())
        );
        for (Order order : expiredOrders) {
            order.setStatus("cancelled");
            order.setCancelledAt(LocalDateTime.now());
            orderMapper.updateById(order);

            Product product = productMapper.selectById(order.getProductId());
            if (product != null && "sold".equals(product.getStatus())) {
                product.setStatus("on_sale");
                productMapper.updateById(product);
            }
            log.info("Order expired and cancelled: orderNo={}", order.getOrderNo());
        }
    }
}