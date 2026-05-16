package com.campus2hand.auth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("email_tokens")
public class EmailToken {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String token;
    private String type;
    private LocalDateTime expiresAt;
    private Boolean used;
    private LocalDateTime createdAt;
}