package com.campus2hand.order.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefundHandleRequest {
    @NotBlank(message = "处理动作不能为空")
    private String action;

    private String comment;
}