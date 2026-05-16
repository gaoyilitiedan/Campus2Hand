package com.campus2hand.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginHistoryResponse {
    private Long id;
    private LocalDateTime loginAt;
    private String ip;
    private String device;
}