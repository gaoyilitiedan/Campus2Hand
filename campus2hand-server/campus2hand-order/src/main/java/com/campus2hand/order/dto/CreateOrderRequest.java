package com.campus2hand.order.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateOrderRequest {
    @NotNull(message = "商品ID不能为空")
    private Long productId;
}