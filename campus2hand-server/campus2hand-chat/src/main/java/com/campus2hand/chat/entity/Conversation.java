package com.campus2hand.chat.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("conversations")
public class Conversation {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long productId;
    private Long buyerId;
    private Long sellerId;
    private String lastMessage;
    private LocalDateTime lastMessageAt;
    private Integer buyerUnread;
    private Integer sellerUnread;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}