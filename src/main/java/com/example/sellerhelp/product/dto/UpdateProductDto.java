package com.example.sellerhelp.product.dto;

import com.example.sellerhelp.constant.ProductRequestStatus;
import jakarta.validation.constraints.DecimalMin;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class UpdateProductDto {

    private String name;
    private String description;
    private String imageUrl;

    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal unitPrice;

    private Long categoryId;
    private ProductRequestStatus status;
}