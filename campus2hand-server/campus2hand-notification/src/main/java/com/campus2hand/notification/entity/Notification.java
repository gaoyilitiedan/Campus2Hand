package com.campus2hand.notification.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("notifications")
public class Notification {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String type;
    private String title;
    private String content;
    private String targetType;
    private Long targetId;
    @TableField(exist = false)
    private Long orderId;
    @TableField(exist = false)
    private Long productId;
    private Boolean isRead;
    private LocalDateTime createdAt;
}