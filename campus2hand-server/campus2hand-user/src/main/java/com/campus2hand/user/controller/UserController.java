package com.campus2hand.user.controller;

import com.campus2hand.common.dto.ApiResponse;
import com.campus2hand.user.dto.*;
import com.campus2hand.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ApiResponse<UserProfileResponse> getProfile() {
        return ApiResponse.success(userService.getProfile());
    }

    @PutMapping("/me")
    public ApiResponse<UserProfileResponse> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        return ApiResponse.success(userService.updateProfile(request));
    }

    @PutMapping("/me/phone")
    public ApiResponse<Void> bindPhone(@Valid @RequestBody BindPhoneRequest request) {
        userService.bindPhone(request);
        return ApiResponse.success();
    }

    @GetMapping("/{userId}/reputation")
    public ApiResponse<ReputationResponse> getReputation(@PathVariable Long userId) {
        return ApiResponse.success(userService.getReputation(userId));
    }

    @GetMapping("/me/login-history")
    public ApiResponse<List<LoginHistoryResponse>> getLoginHistory() {
        return ApiResponse.success(userService.getLoginHistory());
    }
}