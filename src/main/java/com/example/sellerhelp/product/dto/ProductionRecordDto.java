package com.example.sellerhelp.product.dto;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDate;

@Getter
@Builder
public class ProductionRecordDto {
    private Long id;
    private String productName;
    private String factoryId;
    private Long quantity;
    private LocalDate productionDate;
}