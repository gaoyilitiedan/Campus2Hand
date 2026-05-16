package com.campus2hand.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefreshTokenRequest {
    @NotBlank(message = "RefreshToken不能为空")
    private String refreshToken;
}