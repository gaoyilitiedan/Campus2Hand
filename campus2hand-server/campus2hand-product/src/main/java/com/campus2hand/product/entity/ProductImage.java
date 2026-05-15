package com.campus2hand.product.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("product_images")
public class ProductImage {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long productId;
    private String url;
    @TableField(exist = false)
    private String imageUrl;
    @TableField(exist = false)
    private String thumbnailUrl;
    private Integer sortOrder;
    private LocalDateTime createdAt;
}