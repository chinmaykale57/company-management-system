package com.example.sellerhelp.product.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProductStockDto {
    private String productId;
    private String productName;
    private String factoryId;
    private String factoryName;
    private Long quantity;
}