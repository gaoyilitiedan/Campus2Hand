package com.campus2hand.review.controller;

import com.campus2hand.common.dto.ApiResponse;
import com.campus2hand.common.dto.PageResponse;
import com.campus2hand.review.dto.CreateReviewRequest;
import com.campus2hand.review.dto.ReviewResponse;
import com.campus2hand.review.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ApiResponse<ReviewResponse> create(@Valid @RequestBody CreateReviewRequest request) {
        return ApiResponse.success(reviewService.create(request));
    }

    @GetMapping("/user/{userId}")
    public ApiResponse<PageResponse<ReviewResponse>> getUserReviews(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(reviewService.getUserReviews(userId, page, size));
    }
}