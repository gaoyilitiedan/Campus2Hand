package com.campus2hand.product.dto;

import com.campus2hand.product.entity.Product;
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
public class ProductListResponse {
    private Long id;
    private String title;
    private Long price;
    private Long originalPrice;
    private String category;
    private String condition;
    private String campus;
    private String status;
    private Integer viewCount;
    private Integer favoriteCount;
    private String coverImage;
    private String sellerNickname;
    private Double sellerReputationScore;
    private LocalDateTime createdAt;

    public static ProductListResponse fromEntity(Product product, boolean includeSeller) {
        return ProductListResponse.builder()
                .id(product.getId())
                .title(product.getTitle())
                .price(product.getPrice())
                .originalPrice(product.getOriginalPrice())
                .category(product.getCategory())
                .condition(product.getItemCondition())
                .campus(product.getCampus())
                .status(product.getStatus())
                .viewCount(product.getViewCount())
                .favoriteCount(product.getFavoriteCount())
                .createdAt(product.getCreatedAt())
                .build();
    }
}