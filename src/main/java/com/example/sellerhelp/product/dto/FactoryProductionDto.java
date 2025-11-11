package com.example.sellerhelp.product.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class FactoryProductionDto {
    private Long id;
    private String productId;
    private String productName;
    private String factoryId;
    private String factoryName;
    private Long productionQuantity;
    private LocalDate productionDate;
}