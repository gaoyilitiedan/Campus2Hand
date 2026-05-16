package com.campus2hand.order.controller;

import com.campus2hand.common.dto.ApiResponse;
import com.campus2hand.common.dto.PageResponse;
import com.campus2hand.order.dto.CreateOrderRequest;
import com.campus2hand.order.dto.OrderResponse;
import com.campus2hand.order.dto.RefundHandleRequest;
import com.campus2hand.order.dto.RefundRequest;
import com.campus2hand.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/orders")
    public ApiResponse<OrderResponse> create(@Valid @RequestBody CreateOrderRequest request) {
        return ApiResponse.success(orderService.create(request));
    }

    @GetMapping("/orders/{id}")
    public ApiResponse<OrderResponse> getDetail(@PathVariable Long id) {
        return ApiResponse.success(orderService.getDetail(id));
    }

    @GetMapping("/my/orders")
    public ApiResponse<PageResponse<OrderResponse>> getMyOrders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status) {
        return ApiResponse.success(orderService.getMyOrders(page, size, role, status));
    }

    @PostMapping("/orders/{id}/pay")
    public ApiResponse<Void> pay(@PathVariable Long id) {
        orderService.pay(id);
        return ApiResponse.success();
    }

    @PostMapping("/orders/{id}/ship")
    public ApiResponse<Void> ship(@PathVariable Long id) {
        orderService.ship(id);
        return ApiResponse.success();
    }

    @PostMapping("/orders/{id}/confirm-receipt")
    public ApiResponse<Void> confirmReceipt(@PathVariable Long id) {
        orderService.confirmReceipt(id);
        return ApiResponse.success();
    }

    @PostMapping("/orders/{id}/cancel")
    public ApiResponse<Void> cancel(@PathVariable Long id) {
        orderService.cancel(id);
        return ApiResponse.success();
    }

    @PostMapping("/orders/{id}/refund")
    public ApiResponse<Void> requestRefund(@PathVariable Long id, @Valid @RequestBody RefundRequest request) {
        orderService.requestRefund(id, request);
        return ApiResponse.success();
    }

    @PostMapping("/orders/{id}/refund/handle")
    public ApiResponse<Void> handleRefund(@PathVariable Long id, @Valid @RequestBody RefundHandleRequest request) {
        if ("agree".equals(request.getAction())) {
            orderService.approveRefund(id);
        } else if ("reject".equals(request.getAction())) {
            orderService.rejectRefund(id);
        }
        return ApiResponse.success();
    }
}