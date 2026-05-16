package com.campus2hand.review.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus2hand.auth.entity.User;
import com.campus2hand.auth.mapper.UserMapper;
import com.campus2hand.auth.security.CurrentUserHolder;
import com.campus2hand.common.constant.ErrorCode;
import com.campus2hand.common.dto.PageResponse;
import com.campus2hand.common.exception.BusinessException;
import com.campus2hand.order.entity.Order;
import com.campus2hand.order.mapper.OrderMapper;
import com.campus2hand.review.dto.CreateReviewRequest;
import com.campus2hand.review.dto.ReviewResponse;
import com.campus2hand.review.entity.Review;
import com.campus2hand.review.mapper.ReviewMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewMapper reviewMapper;
    private final OrderMapper orderMapper;
    private final UserMapper userMapper;
    @Transactional
    public ReviewResponse create(CreateReviewRequest request) {
        Long reviewerId = CurrentUserHolder.getUserId();
        Order order = orderMapper.selectById(request.getOrderId());
        if (order == null) throw new BusinessException(ErrorCode.ORDER_NOT_FOUND, "订单不存在");
        if (!"completed".equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.REVIEW_NOT_ALLOWED, "只能评价已完成的订单");
        }
        if (!order.getBuyerId().equals(reviewerId) && !order.getSellerId().equals(reviewerId)) {
            throw new BusinessException(ErrorCode.NO_PERMISSION, "无权评价此订单");
        }
        if (reviewMapper.selectCount(new LambdaQueryWrapper<Review>()
                .eq(Review::getOrderId, request.getOrderId())
                .eq(Review::getReviewerId, reviewerId)) > 0) {
            throw new BusinessException(ErrorCode.ALREADY_REVIEWED, "已评价过此订单");
        }
        Long targetId = order.getBuyerId().equals(reviewerId) ? order.getSellerId() : order.getBuyerId();

        Review review = new Review();
        review.setOrderId(request.getOrderId());
        review.setReviewerId(reviewerId);
        review.setTargetId(targetId);
        review.setRating(request.getRating());
        review.setContent(request.getContent());
        review.setTags(request.getTags() != null ? String.join(",", request.getTags()) : null);
        reviewMapper.insert(review);

        updateReputationScore(targetId);
        return buildResponse(review);
    }

    public PageResponse<ReviewResponse> getUserReviews(Long userId, int page, int size) {
        Page<Review> result = reviewMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<Review>()
                        .eq(Review::getTargetId, userId)
                        .orderByDesc(Review::getCreatedAt)
        );
        List<ReviewResponse> records = result.getRecords().stream()
                .map(this::buildResponse)
                .collect(Collectors.toList());
        return PageResponse.of(records, result.getTotal(), page, size);
    }

    private void updateReputationScore(Long userId) {
        List<Review> reviews = reviewMapper.selectList(
                new LambdaQueryWrapper<Review>().eq(Review::getTargetId, userId)
        );
        if (reviews.isEmpty()) return;
        double avg = reviews.stream().mapToInt(Review::getRating).average().orElse(5.0);
        User user = userMapper.selectById(userId);
        if (user != null) {
            user.setReputationScore(Math.round(avg * 10.0) / 10.0);
            userMapper.updateById(user);
        }
    }

    private ReviewResponse buildResponse(Review review) {
        User reviewer = userMapper.selectById(review.getReviewerId());
        List<String> tags = review.getTags() != null ?
                StrUtil.split(review.getTags(), ",") : List.of();

        return ReviewResponse.builder()
                .id(review.getId())
                .orderId(review.getOrderId())
                .reviewerId(review.getReviewerId())
                .reviewerNickname(reviewer != null ? reviewer.getNickname() : "未知用户")
                .reviewerAvatarUrl(reviewer != null ? reviewer.getAvatarUrl() : null)
                .targetId(review.getTargetId())
                .rating(review.getRating())
                .content(review.getContent())
                .tags(tags)
                .createdAt(review.getCreatedAt())
                .build();
    }
}