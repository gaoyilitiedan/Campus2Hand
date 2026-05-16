package com.campus2hand.dispute.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("disputes")
public class Dispute {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private Long reporterId;
    @TableField(exist = false)
    private Long userId;
    @TableField(exist = false)
    private Long sellerId;
    private String type;
    private String reason;
    @TableField(exist = false)
    private String content;
    private String evidenceUrls;
    private String status;
    private String resolution;
    private Long resolvedBy;
    private LocalDateTime resolvedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}