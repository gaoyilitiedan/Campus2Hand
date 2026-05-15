package com.campus2hand.admin.controller;

import com.campus2hand.admin.dto.AuditRequest;
import com.campus2hand.admin.dto.StatisticsResponse;
import com.campus2hand.admin.service.AdminService;
import com.campus2hand.auth.entity.User;
import com.campus2hand.common.dto.ApiResponse;
import com.campus2hand.common.dto.PageResponse;
import com.campus2hand.dispute.entity.Dispute;
import com.campus2hand.order.dto.OrderResponse;
import com.campus2hand.product.entity.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    public ApiResponse<PageResponse<User>> getUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        return ApiResponse.success(adminService.getUsers(page, size, keyword, status));
    }

    @PutMapping("/users/{id}/lock")
    public ApiResponse<Void> updateUserStatus(@PathVariable Long id, @RequestParam String status) {
        adminService.updateUserStatus(id, status);
        return ApiResponse.success();
    }

    @GetMapping("/users/{id}/orders")
    public ApiResponse<PageResponse<OrderResponse>> getUserOrders(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(adminService.getUserOrders(id, page, size));
    }

    @GetMapping("/products/pending")
    public ApiResponse<PageResponse<Product>> getPendingProducts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(adminService.getPendingProducts(page, size));
    }

    @PutMapping("/products/{id}/audit")
    public ApiResponse<Void> auditProduct(@PathVariable Long id, @RequestBody AuditRequest request) {
        adminService.auditProduct(id, request);
        return ApiResponse.success();
    }

    @GetMapping("/disputes")
    public ApiResponse<PageResponse<Dispute>> getDisputes(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status) {
        return ApiResponse.success(adminService.getDisputes(page, size, status));
    }

    @PutMapping("/disputes/{id}/status")
    public ApiResponse<Void> resolveDispute(@PathVariable Long id, @RequestParam String status, @RequestParam(required = false) String comment) {
        adminService.resolveDispute(id, status, comment);
        return ApiResponse.success();
    }

    @GetMapping("/sensitive-words")
    public ApiResponse<List<String>> getSensitiveWords(@RequestParam(required = false) String keyword) {
        return ApiResponse.success(adminService.getSensitiveWords(keyword));
    }

    @PostMapping("/sensitive-words")
    public ApiResponse<Void> addSensitiveWord(@RequestBody String word) {
        adminService.addSensitiveWord(word);
        return ApiResponse.success();
    }

    @DeleteMapping("/sensitive-words/{id}")
    public ApiResponse<Void> deleteSensitiveWord(@PathVariable Long id) {
        adminService.deleteSensitiveWord(id);
        return ApiResponse.success();
    }

    @GetMapping("/statistics")
    public ApiResponse<StatisticsResponse> getStatistics() {
        return ApiResponse.success(adminService.getStatistics());
    }
}