package com.campus2hand.dispute.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.List;

@Data
public class CreateDisputeRequest {
    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    @NotBlank(message = "类型不能为空")
    private String type;

    @NotBlank(message = "原因不能为空")
    @Size(max = 500, message = "原因最多500字符")
    private String reason;

    private List<String> evidenceUrls;
}