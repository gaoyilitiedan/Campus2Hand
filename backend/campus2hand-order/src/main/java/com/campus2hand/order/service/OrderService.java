package com.campus2hand.order.service;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus2hand.auth.security.CurrentUserHolder;
import com.campus2hand.common.constant.ErrorCode;
import com.campus2hand.common.dto.PageResponse;
import com.campus2hand.common.exception.BusinessException;
import com.campus2hand.order.dto.CreateOrderRequest;
import com.campus2hand.order.dto.OrderResponse;
import com.campus2hand.order.dto.RefundRequest;
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
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderMapper orderMapper;
    private final ProductMapper productMapper;
    private final ProductImageMapper productImageMapper;
    @Transactional
    public OrderResponse create(CreateOrderRequest request) {
        Long buyerId = CurrentUserHolder.getUserId();
        Product product = productMapper.selectById(request.getProductId());
        if (product == null || product.getDeleted()) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, "商品不存在");
        }
        if (!"on_sale".equals(product.getStatus())) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_ON_SALE, "商品已下架或已售出");
        }
        if (product.getUserId().equals(buyerId)) {
            throw new BusinessException(ErrorCode.CANNOT_BUY_OWN, "不能购买自己的商品");
        }
        product.setStatus("sold");
        productMapper.updateById(product);

        Order order = new Order();
        order.setOrderNo(IdUtil.fastSimpleUUID().toUpperCase());
        order.setBuyerId(buyerId);
        order.setSellerId(product.getUserId());
        order.setProductId(product.getId());
        order.setAmount(product.getPrice());
        order.setStatus("pending_payment");
        order.setExpiresAt(LocalDateTime.now().plusMinutes(30));
        orderMapper.insert(order);

        log.info("Order created: orderNo={}, buyerId={}, amount={}", order.getOrderNo(), buyerId, order.getAmount());
        return buildResponse(order);
    }

    public OrderResponse getDetail(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND, "订单不存在");
        }
        Long userId = CurrentUserHolder.getUserId();
        if (!order.getBuyerId().equals(userId) && !order.getSellerId().equals(userId)) {
            throw new BusinessException(ErrorCode.NO_PERMISSION, "无权查看该订单");
        }
        return buildResponse(order);
    }

    public PageResponse<OrderResponse> getMyOrders(int page, int size, String role, String status) {
        Long userId = CurrentUserHolder.getUserId();
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        if ("buyer".equals(role)) {
            wrapper.eq(Order::getBuyerId, userId);
        } else if ("seller".equals(role)) {
            wrapper.eq(Order::getSellerId, userId);
        } else {
            wrapper.and(w -> w.eq(Order::getBuyerId, userId).or().eq(Order::getSellerId, userId));
        }
        if (status != null) wrapper.eq(Order::getStatus, status);
        wrapper.orderByDesc(Order::getCreatedAt);

        Page<Order> result = orderMapper.selectPage(new Page<>(page, size), wrapper);
        List<OrderResponse> records = result.getRecords().stream()
                .map(this::buildResponse)
                .collect(Collectors.toList());
        return PageResponse.of(records, result.getTotal(), page, size);
    }

    @Transactional
    public void pay(Long orderId) {
        Long buyerId = CurrentUserHolder.getUserId();
        Order order = orderMapper.selectById(orderId);
        if (order == null) throw new BusinessException(ErrorCode.ORDER_NOT_FOUND, "订单不存在");
        if (!order.getBuyerId().equals(buyerId)) throw new BusinessException(ErrorCode.NO_PERMISSION, "无权操作");
        if (!"pending_payment".equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_INVALID, "订单状态不允许支付");
        }
        if (LocalDateTime.now().isAfter(order.getExpiresAt())) {
            order.setStatus("cancelled");
            order.setCancelledAt(LocalDateTime.now());
            orderMapper.updateById(order);
            restoreProduct(order.getProductId());
            throw new BusinessException(ErrorCode.ORDER_CANNOT_CANCEL, "订单已过期");
        }
        order.setStatus("paid_escrow");
        order.setPaymentMethod("wechat");
        order.setTransactionId("WX" + IdUtil.fastSimpleUUID().toUpperCase());
        order.setPaidAt(LocalDateTime.now());
        orderMapper.updateById(order);
        log.info("Order paid: orderNo={}", order.getOrderNo());
    }

    @Transactional
    public void ship(Long orderId) {
        Long sellerId = CurrentUserHolder.getUserId();
        Order order = orderMapper.selectById(orderId);
        if (order == null) throw new BusinessException(ErrorCode.ORDER_NOT_FOUND, "订单不存在");
        if (!order.getSellerId().equals(sellerId)) throw new BusinessException(ErrorCode.NO_PERMISSION, "无权操作");
        if (!"paid_escrow".equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_INVALID, "订单状态不允许发货");
        }
        order.setStatus("pending_shipment");
        orderMapper.updateById(order);
        log.info("Order shipped: orderNo={}", order.getOrderNo());
    }

    @Transactional
    public void confirmReceipt(Long orderId) {
        Long buyerId = CurrentUserHolder.getUserId();
        Order order = orderMapper.selectById(orderId);
        if (order == null) throw new BusinessException(ErrorCode.ORDER_NOT_FOUND, "订单不存在");
        if (!order.getBuyerId().equals(buyerId)) throw new BusinessException(ErrorCode.NO_PERMISSION, "无权操作");
        if (!"pending_shipment".equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_INVALID, "订单状态不允许确认收货");
        }
        order.setStatus("completed");
        order.setCompletedAt(LocalDateTime.now());
        orderMapper.updateById(order);
        log.info("Order completed: orderNo={}", order.getOrderNo());
    }

    @Transactional
    public void cancel(Long orderId) {
        Long userId = CurrentUserHolder.getUserId();
        Order order = orderMapper.selectById(orderId);
        if (order == null) throw new BusinessException(ErrorCode.ORDER_NOT_FOUND, "订单不存在");
        if (!order.getBuyerId().equals(userId)) throw new BusinessException(ErrorCode.NO_PERMISSION, "无权操作");
        if (!"pending_payment".equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.ORDER_CANNOT_CANCEL, "只能取消待付款订单");
        }
        order.setStatus("cancelled");
        order.setCancelledAt(LocalDateTime.now());
        orderMapper.updateById(order);
        restoreProduct(order.getProductId());
        log.info("Order cancelled: orderNo={}", order.getOrderNo());
    }

    @Transactional
    public void requestRefund(Long orderId, RefundRequest request) {
        Long buyerId = CurrentUserHolder.getUserId();
        Order order = orderMapper.selectById(orderId);
        if (order == null) throw new BusinessException(ErrorCode.ORDER_NOT_FOUND, "订单不存在");
        if (!order.getBuyerId().equals(buyerId)) throw new BusinessException(ErrorCode.NO_PERMISSION, "无权操作");
        if (!"pending_shipment".equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.ORDER_CANNOT_REFUND, "当前状态不允许退款");
        }
        order.setStatus("refunding");
        order.setRefundReason(request.getReason());
        order.setRefundAmount(order.getAmount());
        order.setRefundRequestedAt(LocalDateTime.now());
        orderMapper.updateById(order);
        log.info("Refund requested: orderNo={}", order.getOrderNo());
    }

    @Transactional
    public void approveRefund(Long orderId) {
        Long sellerId = CurrentUserHolder.getUserId();
        Order order = orderMapper.selectById(orderId);
        if (order == null) throw new BusinessException(ErrorCode.ORDER_NOT_FOUND, "订单不存在");
        if (!order.getSellerId().equals(sellerId)) throw new BusinessException(ErrorCode.NO_PERMISSION, "无权操作");
        if (!"refunding".equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_INVALID, "当前状态不允许处理退款");
        }
        order.setStatus("refunded");
        order.setRefundId("RF" + IdUtil.fastSimpleUUID().toUpperCase());
        order.setRefundCompletedAt(LocalDateTime.now());
        orderMapper.updateById(order);
        restoreProduct(order.getProductId());
        log.info("Refund approved: orderNo={}", order.getOrderNo());
    }

    @Transactional
    public void rejectRefund(Long orderId) {
        Long sellerId = CurrentUserHolder.getUserId();
        Order order = orderMapper.selectById(orderId);
        if (order == null) throw new BusinessException(ErrorCode.ORDER_NOT_FOUND, "订单不存在");
        if (!order.getSellerId().equals(sellerId)) throw new BusinessException(ErrorCode.NO_PERMISSION, "无权操作");
        if (!"refunding".equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_INVALID, "当前状态不允许处理退款");
        }
        order.setStatus("pending_shipment");
        order.setRefundReason(null);
        order.setRefundAmount(null);
        order.setRefundRequestedAt(null);
        orderMapper.updateById(order);
        log.info("Refund rejected: orderNo={}", order.getOrderNo());
    }

    public void handlePayCallback(String body) {
        log.info("Processing pay callback: {}", body);
    }

    private void restoreProduct(Long productId) {
        Product product = productMapper.selectById(productId);
        if (product != null && "sold".equals(product.getStatus())) {
            product.setStatus("on_sale");
            productMapper.updateById(product);
        }
    }

    private OrderResponse buildResponse(Order order) {
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
}