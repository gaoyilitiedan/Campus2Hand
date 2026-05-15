package com.campus2hand.dispute.controller;

import com.campus2hand.common.dto.ApiResponse;
import com.campus2hand.common.dto.PageResponse;
import com.campus2hand.dispute.dto.CreateDisputeRequest;
import com.campus2hand.dispute.dto.DisputeResponse;
import com.campus2hand.dispute.service.DisputeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class DisputeController {

    private final DisputeService disputeService;

    @PostMapping("/disputes")
    public ApiResponse<DisputeResponse> create(@Valid @RequestBody CreateDisputeRequest request) {
        return ApiResponse.success(disputeService.create(request));
    }

    @GetMapping("/disputes/{disputeId}")
    public ApiResponse<DisputeResponse> getDetail(@PathVariable Long disputeId) {
        return ApiResponse.success(disputeService.getDetail(disputeId));
    }

    @GetMapping("/my/disputes")
    public ApiResponse<PageResponse<DisputeResponse>> getMyDisputes(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(disputeService.getMyDisputes(page, size));
    }

    @PostMapping("/disputes/{disputeId}/comments")
    public ApiResponse<Void> addComment(@PathVariable Long disputeId, @RequestBody String content) {
        disputeService.addComment(disputeId, content);
        return ApiResponse.success();
    }
}