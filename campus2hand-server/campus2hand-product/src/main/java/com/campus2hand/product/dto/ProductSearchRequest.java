package com.campus2hand.product.dto;

import lombok.Data;

@Data
public class ProductSearchRequest {
    private String keyword;
    private String category;
    private String condition;
    private String campus;
    private Long minPrice;
    private Long maxPrice;
    private String sortBy = "created_at";
    private String sortOrder = "desc";
    private Integer page = 1;
    private Integer size = 20;
}