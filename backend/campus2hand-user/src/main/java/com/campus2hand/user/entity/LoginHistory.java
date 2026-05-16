package com.campus2hand.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("login_history")
public class LoginHistory {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private LocalDateTime loginAt;
    private String ip;
    private String device;
}