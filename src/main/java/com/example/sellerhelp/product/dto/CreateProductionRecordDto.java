package com.example.sellerhelp.product.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CreateProductionRecordDto {
    @NotBlank(message = "Product ID is required.")
    private String productId;

    @NotBlank(message = "Factory ID is required.")
    private String factoryId;

    @NotNull(message = "Quantity is required.")
    @Min(value = 1, message = "Production quantity must be at least 1.")
    private Long quantity;

    @NotNull(message = "Production date is required.")
    private LocalDate productionDate;
}