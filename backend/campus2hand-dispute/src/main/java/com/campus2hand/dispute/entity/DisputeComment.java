package com.campus2hand.dispute.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("dispute_comments")
public class DisputeComment {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long disputeId;
    private Long userId;
    private String content;
    @TableField(exist = false)
    private String userType;
    private LocalDateTime createdAt;
}