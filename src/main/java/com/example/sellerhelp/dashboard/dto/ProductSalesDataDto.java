package com.example.sellerhelp.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProductSalesDataDto {
    private String productName;
    private Long quantitySold;
}