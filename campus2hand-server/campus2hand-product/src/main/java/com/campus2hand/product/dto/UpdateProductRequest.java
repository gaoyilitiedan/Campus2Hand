package com.campus2hand.product.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UpdateProductRequest {
    @Size(min = 2, max = 60, message = "标题长度2-60字符")
    private String title;

    @Size(min = 10, max = 2000, message = "描述长度10-2000字符")
    private String description;

    @Min(value = 0, message = "价格不能为负数")
    @Max(value = 99999999, message = "价格超出范围")
    private Long price;

    private Long originalPrice;

    private String category;

    private String condition;

    private String campus;
}