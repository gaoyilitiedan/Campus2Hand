package com.campus2hand.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {
    private Long id;
    private String studentId;
    private String nickname;
    private String email;
    private String phone;
    private String avatarUrl;
    private String campus;
    private Double reputationScore;
    private Integer productCount;
    private Integer soldCount;
    private Integer boughtCount;
}