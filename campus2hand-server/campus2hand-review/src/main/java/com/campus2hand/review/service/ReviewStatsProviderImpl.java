package com.campus2hand.review.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus2hand.common.service.ReviewStatsProvider;
import com.campus2hand.review.entity.Review;
import com.campus2hand.review.mapper.ReviewMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReviewStatsProviderImpl implements ReviewStatsProvider {

    private final ReviewMapper reviewMapper;

    @Override
    public long countByTargetId(Long targetId) {
        return reviewMapper.selectCount(
                new LambdaQueryWrapper<Review>()
                        .eq(Review::getTargetId, targetId)
        );
    }
}