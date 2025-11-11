package com.example.sellerhelp.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProductIdSalesDto {
    private Long productId;
    private Long quantitySold;
}