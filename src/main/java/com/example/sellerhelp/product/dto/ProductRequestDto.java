package com.example.sellerhelp.product.dto;

import com.example.sellerhelp.constant.ProductRequestStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class ProductRequestDto {
    private Long id;
    private String requestNumber;
    private String centralOfficerName;
    private String factoryId;
    private String factoryName;
    private ProductRequestStatus status;
    private LocalDateTime createdAt;
    private List<ProductRequestItemDto> products;
}