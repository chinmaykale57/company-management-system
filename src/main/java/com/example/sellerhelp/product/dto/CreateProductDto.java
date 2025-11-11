package com.example.sellerhelp.product.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CreateProductDto {

    @NotBlank(message = "Product name is required.")
    @Size(min = 3, max = 255, message = "Product name must be between 3 and 255 characters.")
    private String name;

    private String description;

    private String imageUrl;

    @NotNull(message = "Unit price is required.")
    @DecimalMin(value = "0.0", inclusive = false, message = "Unit price must be a positive value.")
    private BigDecimal unitPrice;

    @NotNull(message = "Category ID is required.")
    private Long categoryId;
}