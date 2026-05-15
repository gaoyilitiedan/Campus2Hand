package com.campus2hand.product.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.List;

@Data
public class PublishProductRequest {
    @NotBlank(message = "标题不能为空")
    @Size(min = 2, max = 60, message = "标题长度2-60字符")
    private String title;

    @NotBlank(message = "描述不能为空")
    @Size(min = 10, max = 2000, message = "描述长度10-2000字符")
    private String description;

    @NotNull(message = "价格不能为空")
    @Min(value = 0, message = "价格不能为负数")
    @Max(value = 99999999, message = "价格超出范围")
    private Long price;

    private Long originalPrice;

    @NotBlank(message = "分类不能为空")
    private String category;

    @NotBlank(message = "新旧程度不能为空")
    private String condition;

    @NotBlank(message = "校区不能为空")
    private String campus;

    @NotEmpty(message = "至少上传一张图片")
    @Size(max = 9, message = "最多上传9张图片")
    private List<String> imageUrls;
}