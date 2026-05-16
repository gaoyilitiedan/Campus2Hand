package com.campus2hand.product.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("products")
public class Product {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String title;
    private String description;
    private Long price;
    private Long originalPrice;
    private String category;
    private String itemCondition;
    private String campus;
    private String status;
    private Integer viewCount;
    private Integer favoriteCount;
    @Version
    private Integer version;
    @TableLogic
    private Boolean deleted;
    private String auditReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}