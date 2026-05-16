package com.campus2hand.product.controller;

import com.campus2hand.common.dto.ApiResponse;
import com.campus2hand.common.dto.PageResponse;
import com.campus2hand.product.dto.*;
import com.campus2hand.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ApiResponse<ProductDetailResponse> publish(@Valid @RequestBody PublishProductRequest request) {
        return ApiResponse.success(productService.publish(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<ProductDetailResponse> getDetail(@PathVariable Long id) {
        return ApiResponse.success(productService.getDetail(id));
    }

    @PutMapping("/{id}")
    public ApiResponse<ProductDetailResponse> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductRequest request) {
        return ApiResponse.success(productService.updateProduct(id, request));
    }

    @GetMapping("/search")
    public ApiResponse<PageResponse<ProductListResponse>> search(@Valid ProductSearchRequest request) {
        return ApiResponse.success(productService.search(request));
    }

    @GetMapping("/my")
    public ApiResponse<PageResponse<ProductListResponse>> getMyProducts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status) {
        return ApiResponse.success(productService.getMyProducts(page, size, status));
    }

    @PutMapping("/{id}/status")
    public ApiResponse<Void> updateStatus(@PathVariable Long id, @RequestParam String status) {
        productService.updateStatus(id, status);
        return ApiResponse.success();
    }

    @PostMapping("/{id}/delist")
    public ApiResponse<Void> delist(@PathVariable Long id) {
        productService.delist(id);
        return ApiResponse.success();
    }

    @PostMapping("/{id}/mark-sold")
    public ApiResponse<Void> markSold(@PathVariable Long id) {
        productService.markSold(id);
        return ApiResponse.success();
    }

    @PostMapping("/{id}/favorite")
    public ApiResponse<Void> toggleFavorite(@PathVariable Long id) {
        productService.toggleFavorite(id);
        return ApiResponse.success();
    }

    @GetMapping("/favorites")
    public ApiResponse<PageResponse<ProductListResponse>> getFavorites(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(productService.getFavorites(page, size));
    }

    @GetMapping("/{id}/seller-reviews")
    public ApiResponse<PageResponse<ProductListResponse>> getSellerReviews(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(productService.getSellerReviews(id, page, size));
    }
}