package com.campus2hand.review.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("reviews")
public class Review {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private Long reviewerId;
    private Long targetId;
    private Integer rating;
    private String content;
    private String tags;
    private LocalDateTime createdAt;
}