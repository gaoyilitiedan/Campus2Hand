package com.campus2hand.product.dto;

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
public class ProductDetailResponse {
    private Long id;
    private Long userId;
    private String sellerNickname;
    private String sellerAvatarUrl;
    private Double sellerReputationScore;
    private String title;
    private String description;
    private Long price;
    private Long originalPrice;
    private String category;
    private String condition;
    private String campus;
    private String status;
    private Integer viewCount;
    private Integer favoriteCount;
    private List<String> imageUrls;
    private Boolean isFavorited;
    private Long totalSold;
    private Long reviewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}