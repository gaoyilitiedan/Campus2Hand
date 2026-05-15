package com.campus2hand.auth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("email_verifications")
public class EmailVerification {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String email;
    private String code;
    private String type;
    private LocalDateTime expiresAt;
    private Boolean used;
    private LocalDateTime createdAt;
}