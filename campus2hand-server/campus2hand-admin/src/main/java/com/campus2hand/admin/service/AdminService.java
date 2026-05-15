package com.campus2hand.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus2hand.admin.dto.AuditRequest;
import com.campus2hand.admin.dto.StatisticsResponse;
import com.campus2hand.auth.entity.User;
import com.campus2hand.auth.mapper.UserMapper;
import com.campus2hand.auth.security.CurrentUserHolder;
import com.campus2hand.common.constant.ErrorCode;
import com.campus2hand.common.dto.PageResponse;
import com.campus2hand.common.exception.BusinessException;
import com.campus2hand.dispute.entity.Dispute;
import com.campus2hand.dispute.mapper.DisputeMapper;
import com.campus2hand.order.dto.OrderResponse;
import com.campus2hand.order.entity.Order;
import com.campus2hand.order.mapper.OrderMapper;
import com.campus2hand.product.entity.Product;
import com.campus2hand.product.entity.ProductImage;
import com.campus2hand.product.mapper.ProductImageMapper;
import com.campus2hand.product.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserMapper userMapper;
    private final ProductMapper productMapper;
    private final ProductImageMapper productImageMapper;
    private final OrderMapper orderMapper;
    private final DisputeMapper disputeMapper;
    public PageResponse<User> getUsers(int page, int size, String keyword, String status) {
        CurrentUserHolder.requireAdmin();
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(User::getStudentId, keyword)
                    .or().like(User::getNickname, keyword)
                    .or().like(User::getEmail, keyword));
        }
        if (status != null) wrapper.eq(User::getStatus, status);
        wrapper.orderByDesc(User::getCreatedAt);

        Page<User> result = userMapper.selectPage(new Page<>(page, size), wrapper);
        result.getRecords().forEach(u -> u.setPasswordHash(null));
        return PageResponse.of(result.getRecords(), result.getTotal(), page, size);
    }

    @Transactional
    public void updateUserStatus(Long userId, String status) {
        CurrentUserHolder.requireAdmin();
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException(ErrorCode.USER_NOT_FOUND, "用户不存在");
        user.setStatus(status);
        userMapper.updateById(user);
        log.info("Admin updated user status: userId={}, status={}", userId, status);
    }

    public PageResponse<Product> getPendingProducts(int page, int size) {
        CurrentUserHolder.requireAdmin();
        Page<Product> result = productMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<Product>()
                        .eq(Product::getStatus, "pending_review")
                        .eq(Product::getDeleted, false)
                        .orderByAsc(Product::getCreatedAt)
        );
        return PageResponse.of(result.getRecords(), result.getTotal(), page, size);
    }

    @Transactional
    public void auditProduct(Long productId, AuditRequest request) {
        CurrentUserHolder.requireAdmin();
        Product product = productMapper.selectById(productId);
        if (product == null) throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, "商品不存在");
        if (!"pending_review".equals(product.getStatus())) {
            throw new BusinessException(ErrorCode.PRODUCT_AUDIT_REJECTED, "商品不在待审核状态");
        }
        if ("approve".equals(request.getAction())) {
            product.setStatus("on_sale");
            product.setAuditReason(null);
        } else if ("reject".equals(request.getAction())) {
            if (request.getReason() == null || request.getReason().isBlank()) {
                throw new BusinessException(ErrorCode.ADMIN_AUDIT_REASON_REQUIRED, "驳回时必须填写原因");
            }
            product.setStatus("delisted");
            product.setAuditReason(request.getReason());
        }
        productMapper.updateById(product);
        log.info("Admin audited product: productId={}, action={}", productId, request.getAction());
    }

    public PageResponse<Dispute> getDisputes(int page, int size, String status) {
        CurrentUserHolder.requireAdmin();
        LambdaQueryWrapper<Dispute> wrapper = new LambdaQueryWrapper<>();
        if (status != null) wrapper.eq(Dispute::getStatus, status);
        wrapper.orderByAsc(Dispute::getCreatedAt);

        Page<Dispute> result = disputeMapper.selectPage(new Page<>(page, size), wrapper);
        return PageResponse.of(result.getRecords(), result.getTotal(), page, size);
    }

    @Transactional
    public void resolveDispute(Long disputeId, String status, String comment) {
        CurrentUserHolder.requireAdmin();
        Long adminId = CurrentUserHolder.getUserId();
        Dispute dispute = disputeMapper.selectById(disputeId);
        if (dispute == null) throw new BusinessException(ErrorCode.DISPUTE_NOT_FOUND, "工单不存在");
        dispute.setStatus(status);
        dispute.setResolution(comment);
        dispute.setResolvedBy(adminId);
        dispute.setResolvedAt(LocalDateTime.now());
        disputeMapper.updateById(dispute);
        log.info("Admin resolved dispute: disputeId={}, status={}", disputeId, status);
    }

    public PageResponse<OrderResponse> getUserOrders(Long userId, int page, int size) {
        CurrentUserHolder.requireAdmin();
        Page<Order> result = orderMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<Order>()
                        .and(w -> w.eq(Order::getBuyerId, userId).or().eq(Order::getSellerId, userId))
                        .orderByDesc(Order::getCreatedAt)
        );
        List<OrderResponse> records = result.getRecords().stream()
                .map(this::buildOrderResponse)
                .collect(Collectors.toList());
        return PageResponse.of(records, result.getTotal(), page, size);
    }

    public List<String> getSensitiveWords(String keyword) {
        CurrentUserHolder.requireAdmin();
        return new ArrayList<>();
    }

    public void addSensitiveWord(String word) {
        CurrentUserHolder.requireAdmin();
        log.info("Admin added sensitive word: {}", word);
    }

    public void deleteSensitiveWord(Long id) {
        CurrentUserHolder.requireAdmin();
        log.info("Admin deleted sensitive word: id={}", id);
    }

    private OrderResponse buildOrderResponse(Order order) {
        Product product = productMapper.selectById(order.getProductId());
        String coverImage = productImageMapper.selectList(
                new LambdaQueryWrapper<ProductImage>()
                        .eq(ProductImage::getProductId, order.getProductId())
                        .orderByAsc(ProductImage::getSortOrder)
                        .last("LIMIT 1")
        ).stream().findFirst().map(ProductImage::getUrl).orElse(null);

        return OrderResponse.builder()
                .id(order.getId())
                .orderNo(order.getOrderNo())
                .buyerId(order.getBuyerId())
                .sellerId(order.getSellerId())
                .productId(order.getProductId())
                .productTitle(product != null ? product.getTitle() : "已删除")
                .productCoverImage(coverImage)
                .amount(order.getAmount())
                .status(order.getStatus())
                .paymentMethod(order.getPaymentMethod())
                .transactionId(order.getTransactionId())
                .refundAmount(order.getRefundAmount())
                .refundReason(order.getRefundReason())
                .refundRequestedAt(order.getRefundRequestedAt())
                .refundCompletedAt(order.getRefundCompletedAt())
                .paidAt(order.getPaidAt())
                .completedAt(order.getCompletedAt())
                .cancelledAt(order.getCancelledAt())
                .expiresAt(order.getExpiresAt())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    public StatisticsResponse getStatistics() {
        CurrentUserHolder.requireAdmin();
        long totalUsers = userMapper.selectCount(null);
        long activeUsers = userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getStatus, "active"));
        long totalProducts = productMapper.selectCount(new LambdaQueryWrapper<Product>().eq(Product::getDeleted, false));
        long onSaleProducts = productMapper.selectCount(new LambdaQueryWrapper<Product>()
                .eq(Product::getStatus, "on_sale").eq(Product::getDeleted, false));
        long totalOrders = orderMapper.selectCount(null);
        long completedOrders = orderMapper.selectCount(new LambdaQueryWrapper<Order>().eq(Order::getStatus, "completed"));
        long pendingDisputes = disputeMapper.selectCount(new LambdaQueryWrapper<Dispute>().eq(Dispute::getStatus, "pending"));
        long totalRevenue = orderMapper.selectList(new LambdaQueryWrapper<Order>().eq(Order::getStatus, "completed"))
                .stream().mapToLong(Order::getAmount).sum();

        return StatisticsResponse.builder()
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .totalProducts(totalProducts)
                .onSaleProducts(onSaleProducts)
                .totalOrders(totalOrders)
                .completedOrders(completedOrders)
                .pendingDisputes(pendingDisputes)
                .totalRevenue(totalRevenue)
                .build();
    }
}