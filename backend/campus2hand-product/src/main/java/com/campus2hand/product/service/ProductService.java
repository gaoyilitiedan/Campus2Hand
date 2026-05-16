package com.campus2hand.product.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus2hand.auth.entity.User;
import com.campus2hand.auth.mapper.UserMapper;
import com.campus2hand.auth.security.CurrentUserHolder;
import com.campus2hand.common.constant.ErrorCode;
import com.campus2hand.common.dto.PageResponse;
import com.campus2hand.common.exception.BusinessException;
import com.campus2hand.common.service.SellerStatsProvider;
import com.campus2hand.common.service.ReviewStatsProvider;
import com.campus2hand.product.dto.ProductDetailResponse;
import com.campus2hand.product.dto.ProductListResponse;
import com.campus2hand.product.dto.ProductSearchRequest;
import com.campus2hand.product.dto.PublishProductRequest;
import com.campus2hand.product.dto.UpdateProductRequest;
import com.campus2hand.product.entity.Favorite;
import com.campus2hand.product.entity.Product;
import com.campus2hand.product.entity.ProductImage;
import com.campus2hand.product.mapper.FavoriteMapper;
import com.campus2hand.product.mapper.ProductImageMapper;
import com.campus2hand.product.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductMapper productMapper;
    private final ProductImageMapper productImageMapper;
    private final FavoriteMapper favoriteMapper;
    private final UserMapper userMapper;
    private final SellerStatsProvider sellerStatsProvider;
    private final ReviewStatsProvider reviewStatsProvider;

    @Transactional
    public ProductDetailResponse publish(PublishProductRequest request) {
        Long userId = CurrentUserHolder.getUserId();
        Product product = new Product();
        product.setUserId(userId);
        product.setTitle(request.getTitle());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setOriginalPrice(request.getOriginalPrice());
        product.setCategory(request.getCategory());
        product.setItemCondition(request.getCondition());
        product.setCampus(request.getCampus());
        product.setStatus("pending_review");
        productMapper.insert(product);

        for (int i = 0; i < request.getImageUrls().size(); i++) {
            ProductImage image = new ProductImage();
            image.setProductId(product.getId());
            image.setUrl(request.getImageUrls().get(i));
            image.setSortOrder(i);
            productImageMapper.insert(image);
        }
        log.info("Product published: id={}, title={}", product.getId(), product.getTitle());
        return buildDetail(product, false);
    }

    public ProductDetailResponse getDetail(Long productId) {
        Product product = productMapper.selectById(productId);
        if (product == null || product.getDeleted()) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, "商品不存在");
        }
        product.setViewCount(product.getViewCount() + 1);
        productMapper.updateById(product);

        boolean isFavorited = false;
        try {
            Long userId = CurrentUserHolder.getUserId();
            isFavorited = favoriteMapper.selectCount(
                    new LambdaQueryWrapper<Favorite>()
                            .eq(Favorite::getUserId, userId)
                            .eq(Favorite::getProductId, productId)
            ) > 0;
        } catch (BusinessException ignored) {
        }
        return buildDetail(product, isFavorited);
    }

    public PageResponse<ProductListResponse> search(ProductSearchRequest request) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getDeleted, false);
        wrapper.eq(Product::getStatus, "on_sale");

        if (request.getKeyword() != null && !request.getKeyword().isBlank()) {
            wrapper.and(w -> w.like(Product::getTitle, request.getKeyword())
                    .or().like(Product::getDescription, request.getKeyword()));
        }
        if (request.getCategory() != null) wrapper.eq(Product::getCategory, request.getCategory());
        if (request.getCondition() != null) wrapper.eq(Product::getItemCondition, request.getCondition());
        if (request.getCampus() != null) wrapper.eq(Product::getCampus, request.getCampus());
        if (request.getMinPrice() != null) wrapper.ge(Product::getPrice, request.getMinPrice());
        if (request.getMaxPrice() != null) wrapper.le(Product::getPrice, request.getMaxPrice());

        if ("price".equals(request.getSortBy())) {
            wrapper.orderBy(true, "asc".equals(request.getSortOrder()), Product::getPrice);
        } else {
            wrapper.orderByDesc(Product::getCreatedAt);
        }

        Page<Product> page = new Page<>(request.getPage(), request.getSize());
        Page<Product> result = productMapper.selectPage(page, wrapper);

        List<ProductListResponse> records = result.getRecords().stream()
                .map(this::buildListResponse)
                .collect(Collectors.toList());

        return PageResponse.of(records, result.getTotal(), request.getPage(), request.getSize());
    }

    public PageResponse<ProductListResponse> getMyProducts(int page, int size, String status) {
        Long userId = CurrentUserHolder.getUserId();
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getUserId, userId);
        wrapper.eq(Product::getDeleted, false);
        if (status != null) wrapper.eq(Product::getStatus, status);
        wrapper.orderByDesc(Product::getCreatedAt);

        Page<Product> result = productMapper.selectPage(new Page<>(page, size), wrapper);
        List<ProductListResponse> records = result.getRecords().stream()
                .map(this::buildListResponse)
                .collect(Collectors.toList());
        return PageResponse.of(records, result.getTotal(), page, size);
    }

    @Transactional
    public void updateStatus(Long productId, String status) {
        Long userId = CurrentUserHolder.getUserId();
        Product product = productMapper.selectById(productId);
        if (product == null || product.getDeleted()) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, "商品不存在");
        }
        if (!product.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NO_PERMISSION, "无权操作该商品");
        }
        product.setStatus(status);
        productMapper.updateById(product);
    }

    @Transactional
    public void toggleFavorite(Long productId) {
        Long userId = CurrentUserHolder.getUserId();
        Product product = productMapper.selectById(productId);
        if (product == null || product.getDeleted()) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, "商品不存在");
        }
        Favorite existing = favoriteMapper.selectOne(
                new LambdaQueryWrapper<Favorite>()
                        .eq(Favorite::getUserId, userId)
                        .eq(Favorite::getProductId, productId)
        );
        if (existing != null) {
            favoriteMapper.deleteById(existing.getId());
            product.setFavoriteCount(Math.max(0, product.getFavoriteCount() - 1));
        } else {
            Favorite fav = new Favorite();
            fav.setUserId(userId);
            fav.setProductId(productId);
            favoriteMapper.insert(fav);
            product.setFavoriteCount(product.getFavoriteCount() + 1);
        }
        productMapper.updateById(product);
    }

    @Transactional
    public ProductDetailResponse updateProduct(Long productId, UpdateProductRequest request) {
        Long userId = CurrentUserHolder.getUserId();
        Product product = productMapper.selectById(productId);
        if (product == null || product.getDeleted()) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, "商品不存在");
        }
        if (!product.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NO_PERMISSION, "无权操作该商品");
        }
        if (!"on_sale".equals(product.getStatus()) && !"pending_review".equals(product.getStatus())) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_ON_SALE, "当前状态不允许编辑");
        }
        if (request.getTitle() != null) product.setTitle(request.getTitle());
        if (request.getDescription() != null) product.setDescription(request.getDescription());
        if (request.getPrice() != null) product.setPrice(request.getPrice());
        if (request.getOriginalPrice() != null) product.setOriginalPrice(request.getOriginalPrice());
        if (request.getCategory() != null) product.setCategory(request.getCategory());
        if (request.getCondition() != null) product.setItemCondition(request.getCondition());
        if (request.getCampus() != null) product.setCampus(request.getCampus());
        product.setStatus("pending_review");
        productMapper.updateById(product);
        return buildDetail(product, false);
    }

    @Transactional
    public void delist(Long productId) {
        Long userId = CurrentUserHolder.getUserId();
        Product product = productMapper.selectById(productId);
        if (product == null || product.getDeleted()) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, "商品不存在");
        }
        if (!product.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NO_PERMISSION, "无权操作该商品");
        }
        UpdateWrapper<Product> wrapper = new UpdateWrapper<>();
        wrapper.eq("id", productId)
                .eq("version", product.getVersion())
                .set("status", "delisted")
                .setSql("version = version + 1");
        int updated = productMapper.update(null, wrapper);
        if (updated == 0) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_ON_SALE, "操作失败，商品状态已变更");
        }
    }

    @Transactional
    public void markSold(Long productId) {
        Long userId = CurrentUserHolder.getUserId();
        Product product = productMapper.selectById(productId);
        if (product == null || product.getDeleted()) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, "商品不存在");
        }
        if (!product.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NO_PERMISSION, "无权操作该商品");
        }
        UpdateWrapper<Product> wrapper = new UpdateWrapper<>();
        wrapper.eq("id", productId)
                .eq("version", product.getVersion())
                .set("status", "sold")
                .setSql("version = version + 1");
        int updated = productMapper.update(null, wrapper);
        if (updated == 0) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_ON_SALE, "操作失败，商品状态已变更");
        }
    }

    public long getSellerReviewCount(Long productId) {
        Product product = productMapper.selectById(productId);
        if (product == null || product.getDeleted()) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, "商品不存在");
        }
        return reviewStatsProvider.countByTargetId(product.getUserId());
    }

    public PageResponse<ProductListResponse> getSellerReviews(Long productId, int page, int size) {
        Product product = productMapper.selectById(productId);
        if (product == null || product.getDeleted()) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, "商品不存在");
        }
        Page<Product> result = productMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<Product>()
                        .eq(Product::getUserId, product.getUserId())
                        .eq(Product::getStatus, "on_sale")
                        .eq(Product::getDeleted, false)
                        .orderByDesc(Product::getCreatedAt)
        );
        List<ProductListResponse> records = result.getRecords().stream()
                .map(p -> buildListResponse(p))
                .collect(Collectors.toList());
        return PageResponse.of(records, result.getTotal(), page, size);
    }

    public PageResponse<ProductListResponse> getFavorites(int page, int size) {
        Long userId = CurrentUserHolder.getUserId();
        Page<Favorite> favPage = favoriteMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<Favorite>()
                        .eq(Favorite::getUserId, userId)
                        .orderByDesc(Favorite::getCreatedAt)
        );
        List<Long> productIds = favPage.getRecords().stream()
                .map(Favorite::getProductId).collect(Collectors.toList());
        if (productIds.isEmpty()) {
            return PageResponse.of(List.of(), 0, page, size);
        }
        List<ProductListResponse> records = favPage.getRecords().stream()
                .map(fav -> {
                    Product p = productMapper.selectById(fav.getProductId());
                    return p != null ? ProductListResponse.fromEntity(p, false) : null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        return PageResponse.of(records, favPage.getTotal(), page, size);
    }

    private ProductDetailResponse buildDetail(Product product, boolean isFavorited) {
        User seller = userMapper.selectById(product.getUserId());
        List<String> imageUrls = productImageMapper.selectList(
                new LambdaQueryWrapper<ProductImage>()
                        .eq(ProductImage::getProductId, product.getId())
                        .orderByAsc(ProductImage::getSortOrder)
        ).stream().map(ProductImage::getUrl).collect(Collectors.toList());

        long totalSold = sellerStatsProvider.countCompletedSales(product.getUserId());
        long reviewCount = reviewStatsProvider.countByTargetId(product.getUserId());

        return ProductDetailResponse.builder()
                .id(product.getId())
                .userId(product.getUserId())
                .sellerNickname(seller != null ? seller.getNickname() : "未知用户")
                .sellerAvatarUrl(seller != null ? seller.getAvatarUrl() : null)
                .sellerReputationScore(seller != null ? seller.getReputationScore() : 5.0)
                .title(product.getTitle())
                .description(product.getDescription())
                .price(product.getPrice())
                .originalPrice(product.getOriginalPrice())
                .category(product.getCategory())
                .condition(product.getItemCondition())
                .campus(product.getCampus())
                .status(product.getStatus())
                .viewCount(product.getViewCount())
                .favoriteCount(product.getFavoriteCount())
                .imageUrls(imageUrls)
                .isFavorited(isFavorited)
                .totalSold(totalSold)
                .reviewCount(reviewCount)
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    private ProductListResponse buildListResponse(Product product) {
        User seller = userMapper.selectById(product.getUserId());
        String coverImage = productImageMapper.selectList(
                new LambdaQueryWrapper<ProductImage>()
                        .eq(ProductImage::getProductId, product.getId())
                        .orderByAsc(ProductImage::getSortOrder)
                        .last("LIMIT 1")
        ).stream().findFirst().map(ProductImage::getUrl).orElse(null);

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
                .coverImage(coverImage)
                .sellerNickname(seller != null ? seller.getNickname() : "未知用户")
                .sellerReputationScore(seller != null ? seller.getReputationScore() : 5.0)
                .createdAt(product.getCreatedAt())
                .build();
    }
}