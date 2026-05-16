package com.campus2hand.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RefundRequest {
    @NotBlank(message = "退款原因不能为空")
    @Size(max = 500, message = "退款原因最多500字符")
    private String reason;
}