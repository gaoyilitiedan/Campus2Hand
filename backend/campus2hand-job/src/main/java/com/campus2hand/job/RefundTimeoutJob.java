package com.campus2hand.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus2hand.dispute.entity.Dispute;
import com.campus2hand.dispute.mapper.DisputeMapper;
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
public class RefundTimeoutJob {

    private final OrderMapper orderMapper;
    private final DisputeMapper disputeMapper;

    @Scheduled(fixedRate = 300000)
    public void upgradeRefundTimeoutToDispute() {
        LocalDateTime fortyEightHoursAgo = LocalDateTime.now().minusHours(48);
        
        List<Order> pendingRefunds = orderMapper.selectList(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getStatus, "refunding")
                        .lt(Order::getUpdatedAt, fortyEightHoursAgo)
        );

        for (Order order : pendingRefunds) {
            LambdaQueryWrapper<Dispute> existingDispute = new LambdaQueryWrapper<>();
            existingDispute.eq(Dispute::getOrderId, order.getId());
            existingDispute.ne(Dispute::getStatus, "resolved");
            
            if (disputeMapper.selectCount(existingDispute) == 0) {
                Dispute dispute = new Dispute();
                dispute.setOrderId(order.getId());
                dispute.setUserId(order.getBuyerId());
                dispute.setSellerId(order.getSellerId());
                dispute.setType("refund_timeout");
                dispute.setStatus("pending");
                dispute.setContent("退款申请超过48小时未处理，自动升级为纠纷工单");
                dispute.setCreatedAt(LocalDateTime.now());
                disputeMapper.insert(dispute);
                
                log.info("Refund timeout upgraded to dispute: orderNo={}, disputeId={}", 
                        order.getOrderNo(), dispute.getId());
            }
        }
    }
}