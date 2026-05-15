package com.campus2hand.order.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus2hand.common.service.SellerStatsProvider;
import com.campus2hand.order.entity.Order;
import com.campus2hand.order.mapper.OrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SellerStatsProviderImpl implements SellerStatsProvider {

    private final OrderMapper orderMapper;

    @Override
    public long countCompletedSales(Long sellerId) {
        return orderMapper.selectCount(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getSellerId, sellerId)
                        .eq(Order::getStatus, "completed")
        );
    }
}