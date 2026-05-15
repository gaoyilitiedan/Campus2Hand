package com.campus2hand.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus2hand.dispute.entity.Dispute;
import com.campus2hand.dispute.mapper.DisputeMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DisputeTimeoutJob {

    private final DisputeMapper disputeMapper;

    @Scheduled(fixedRate = 3600000)
    public void alertUnprocessedDisputes() {
        LocalDateTime seventyTwoHoursAgo = LocalDateTime.now().minusHours(72);
        
        List<Dispute> unprocessedDisputes = disputeMapper.selectList(
                new LambdaQueryWrapper<Dispute>()
                        .eq(Dispute::getStatus, "pending")
                        .lt(Dispute::getCreatedAt, seventyTwoHoursAgo)
        );

        for (Dispute dispute : unprocessedDisputes) {
            log.error("Dispute timeout alert: disputeId={}, orderId={}, createdAt={}", 
                    dispute.getId(), dispute.getOrderId(), dispute.getCreatedAt());
        }
        
        if (!unprocessedDisputes.isEmpty()) {
            log.error("Total {} disputes pending for more than 72 hours", unprocessedDisputes.size());
        }
    }
}