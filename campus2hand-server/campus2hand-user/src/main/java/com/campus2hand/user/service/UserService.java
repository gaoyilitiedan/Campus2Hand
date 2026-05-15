package com.campus2hand.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus2hand.auth.entity.User;
import com.campus2hand.auth.mapper.UserMapper;
import com.campus2hand.auth.security.CurrentUserHolder;
import com.campus2hand.common.constant.ErrorCode;
import com.campus2hand.common.exception.BusinessException;
import com.campus2hand.order.entity.Order;
import com.campus2hand.order.mapper.OrderMapper;
import com.campus2hand.product.entity.Product;
import com.campus2hand.product.mapper.ProductMapper;
import com.campus2hand.review.entity.Review;
import com.campus2hand.review.mapper.ReviewMapper;
import com.campus2hand.user.dto.*;
import com.campus2hand.user.entity.LoginHistory;
import com.campus2hand.user.mapper.LoginHistoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final ProductMapper productMapper;
    private final OrderMapper orderMapper;
    private final ReviewMapper reviewMapper;
    private final LoginHistoryMapper loginHistoryMapper;
    public UserProfileResponse getProfile() {
        Long userId = CurrentUserHolder.getUserId();
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, "用户不存在");
        }
        long productCount = productMapper.selectCount(
                new LambdaQueryWrapper<Product>().eq(Product::getUserId, userId)
        );
        long soldCount = orderMapper.selectCount(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getSellerId, userId)
                        .eq(Order::getStatus, "completed")
        );
        long boughtCount = orderMapper.selectCount(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getBuyerId, userId)
                        .eq(Order::getStatus, "completed")
        );
        return UserProfileResponse.builder()
                .id(user.getId())
                .studentId(maskStudentId(user.getStudentId()))
                .nickname(user.getNickname())
                .email(maskEmail(user.getEmail()))
                .phone(maskPhone(user.getPhone()))
                .avatarUrl(user.getAvatarUrl())
                .campus(user.getCampus())
                .reputationScore(user.getReputationScore())
                .productCount((int) productCount)
                .soldCount((int) soldCount)
                .boughtCount((int) boughtCount)
                .build();
    }

    public UserProfileResponse updateProfile(UpdateProfileRequest request) {
        Long userId = CurrentUserHolder.getUserId();
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, "用户不存在");
        }
        if (request.getNickname() != null) user.setNickname(request.getNickname());
        if (request.getAvatarUrl() != null) user.setAvatarUrl(request.getAvatarUrl());
        if (request.getCampus() != null) user.setCampus(request.getCampus());
        userMapper.updateById(user);
        return getProfile();
    }

    public void bindPhone(BindPhoneRequest request) {
        Long userId = CurrentUserHolder.getUserId();
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, "用户不存在");
        }
        user.setPhone(request.getPhone());
        userMapper.updateById(user);
    }

    public ReputationResponse getReputation(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, "用户不存在");
        }

        List<Review> reviews = reviewMapper.selectList(
                new LambdaQueryWrapper<Review>().eq(Review::getTargetId, userId)
        );

        Map<Integer, Long> distribution = reviews.stream()
                .collect(Collectors.groupingBy(Review::getRating, Collectors.counting()));

        List<ReputationResponse.RecentReview> recentReviews = reviews.stream()
                .sorted(Comparator.comparing(Review::getCreatedAt).reversed())
                .limit(5)
                .map(r -> {
                    User reviewer = userMapper.selectById(r.getReviewerId());
                    return ReputationResponse.RecentReview.builder()
                            .id(r.getId())
                            .reviewerName(reviewer != null ? reviewer.getNickname() : "匿名用户")
                            .rating(r.getRating())
                            .content(r.getContent())
                            .createdAt(r.getCreatedAt() != null ? r.getCreatedAt().toString() : null)
                            .build();
                })
                .collect(Collectors.toList());

        return ReputationResponse.builder()
                .score(user.getReputationScore())
                .distribution(distribution)
                .recentReviews(recentReviews)
                .build();
    }

    public List<LoginHistoryResponse> getLoginHistory() {
        Long userId = CurrentUserHolder.getUserId();
        Page<LoginHistory> page = new Page<>(1, 10);
        Page<LoginHistory> result = loginHistoryMapper.selectPage(page,
                new LambdaQueryWrapper<LoginHistory>()
                        .eq(LoginHistory::getUserId, userId)
                        .orderByDesc(LoginHistory::getLoginAt)
        );
        return result.getRecords().stream()
                .map(h -> LoginHistoryResponse.builder()
                        .id(h.getId())
                        .loginAt(h.getLoginAt())
                        .ip(h.getIp())
                        .device(h.getDevice())
                        .build())
                .collect(Collectors.toList());
    }

    private String maskStudentId(String studentId) {
        if (studentId == null || studentId.length() <= 4) return studentId;
        return studentId.substring(0, 2) + "****" + studentId.substring(studentId.length() - 2);
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) return email;
        int atIndex = email.indexOf("@");
        String prefix = email.substring(0, atIndex);
        String suffix = email.substring(atIndex);
        if (prefix.length() <= 2) return prefix + "***" + suffix;
        return prefix.substring(0, 2) + "***" + suffix;
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) return phone;
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }
}