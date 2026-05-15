package com.campus2hand.review.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponse {
    private Long id;
    private Long orderId;
    private Long reviewerId;
    private String reviewerNickname;
    private String reviewerAvatarUrl;
    private Long targetId;
    private Integer rating;
    private String content;
    private List<String> tags;
    private LocalDateTime createdAt;
}