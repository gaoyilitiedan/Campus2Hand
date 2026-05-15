package com.campus2hand.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SendMessageRequest {
    @NotBlank(message = "消息内容不能为空")
    @Size(max = 1000, message = "消息内容最多1000字符")
    private String content;

    private String type = "text";
}