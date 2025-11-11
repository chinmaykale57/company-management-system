package com.example.sellerhelp.product.dto;

import com.example.sellerhelp.constant.ProductRequestStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class ProductDto {
    private String productId;
    private String name;
    private String description;
    private String imageUrl;
    private BigDecimal unitPrice;
    private String categoryName;
    private ProductRequestStatus status;
    private String createdBy;
    private String updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}