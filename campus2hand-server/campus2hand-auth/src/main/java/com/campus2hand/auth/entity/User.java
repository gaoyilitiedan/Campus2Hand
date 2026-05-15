package com.campus2hand.auth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("users")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String studentId;
    private String nickname;
    private String passwordHash;
    private String email;
    private Boolean emailVerified;
    private String phone;
    private String avatarUrl;
    private String campus;
    private String role;
    private String status;
    private Double reputationScore;
    private Integer tokenVersion;
    private Integer loginFailCount;
    private LocalDateTime lockedUntil;
    private LocalDateTime lastLoginAt;
    @TableLogic
    private Boolean deleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}