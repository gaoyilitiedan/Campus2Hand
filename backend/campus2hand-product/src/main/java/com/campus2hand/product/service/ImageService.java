package com.campus2hand.product.service;

import com.campus2hand.common.constant.ErrorCode;
import com.campus2hand.common.exception.BusinessException;
import com.campus2hand.product.entity.ProductImage;
import com.campus2hand.product.mapper.ProductImageMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageService {

    private final ProductImageMapper productImageMapper;

    @Value("${app.upload.path:./uploads}")
    private String uploadBasePath;

    @Value("${app.upload.base-url:http://localhost:8080/uploads}")
    private String uploadBaseUrl;

    @Value("${app.upload.max-images:9}")
    private int maxImages;

    @Value("${app.upload.thumbnail-width:200}")
    private int thumbnailWidth;

    @Value("${app.upload.thumbnail-height:200}")
    private int thumbnailHeight;

    public List<ProductImage> uploadImages(Long productId, List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw new BusinessException(ErrorCode.IMAGE_UPLOAD_FAILED, "请至少上传一张图片");
        }
        if (files.size() > maxImages) {
            throw new BusinessException(ErrorCode.IMAGE_COUNT_EXCEED, "图片数量不能超过" + maxImages + "张");
        }

        List<ProductImage> images = new ArrayList<>();
        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            validateImage(file);
            String imageUrl = saveFile(file);
            String thumbnailUrl = generateThumbnail(file);

            ProductImage image = new ProductImage();
            image.setProductId(productId);
            image.setImageUrl(imageUrl);
            image.setThumbnailUrl(thumbnailUrl);
            image.setSortOrder(i);
            productImageMapper.insert(image);
            images.add(image);
        }
        return images;
    }

    public void deleteImagesByProductId(Long productId) {
        List<ProductImage> images = productImageMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ProductImage>()
                        .eq(ProductImage::getProductId, productId)
        );
        for (ProductImage image : images) {
            deleteFile(image.getImageUrl());
            if (image.getThumbnailUrl() != null) {
                deleteFile(image.getThumbnailUrl());
            }
            productImageMapper.deleteById(image.getId());
        }
    }

    private void validateImage(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException(ErrorCode.IMAGE_UPLOAD_FAILED, "上传的图片不能为空");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BusinessException(ErrorCode.IMAGE_UPLOAD_FAILED, "只能上传图片文件");
        }
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new BusinessException(ErrorCode.IMAGE_UPLOAD_FAILED, "图片大小不能超过10MB");
        }
    }

    private String saveFile(MultipartFile file) {
        try {
            String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String fileName = UUID.randomUUID().toString() + getFileExtension(file.getOriginalFilename());
            Path dirPath = Paths.get(uploadBasePath, datePath);
            Files.createDirectories(dirPath);
            Path filePath = dirPath.resolve(fileName);
            file.transferTo(filePath.toFile());
            return uploadBaseUrl + "/" + datePath + "/" + fileName;
        } catch (IOException e) {
            log.error("Failed to save image file", e);
            throw new BusinessException(ErrorCode.IMAGE_UPLOAD_FAILED, "图片上传失败");
        }
    }

    private String generateThumbnail(MultipartFile file) {
        try {
            BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(file.getBytes()));
            if (originalImage == null) {
                return null;
            }
            BufferedImage thumbnail = new BufferedImage(thumbnailWidth, thumbnailHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = thumbnail.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(originalImage, 0, 0, thumbnailWidth, thumbnailHeight, null);
            g.dispose();

            String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String fileName = "thumb_" + UUID.randomUUID() + ".jpg";
            Path dirPath = Paths.get(uploadBasePath, datePath);
            Files.createDirectories(dirPath);
            Path filePath = dirPath.resolve(fileName);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(thumbnail, "jpg", baos);
            Files.write(filePath, baos.toByteArray());

            return uploadBaseUrl + "/" + datePath + "/" + fileName;
        } catch (IOException e) {
            log.warn("Failed to generate thumbnail", e);
            return null;
        }
    }

    private void deleteFile(String fileUrl) {
        if (fileUrl == null || !fileUrl.startsWith(uploadBaseUrl)) {
            return;
        }
        try {
            String relativePath = fileUrl.substring(uploadBaseUrl.length() + 1);
            Path filePath = Paths.get(uploadBasePath, relativePath);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.warn("Failed to delete file: {}", fileUrl, e);
        }
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return ".jpg";
        }
        return fileName.substring(fileName.lastIndexOf("."));
    }
}