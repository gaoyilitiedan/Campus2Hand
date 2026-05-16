package com.campus2hand.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReputationResponse {
    private Double score;
    private Map<Integer, Long> distribution;
    private List<RecentReview> recentReviews;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentReview {
        private Long id;
        private String reviewerName;
        private Integer rating;
        private String content;
        private String createdAt;
    }
}