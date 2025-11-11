package com.example.sellerhelp.product.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class RecordProductionDto {

    @NotBlank(message = "Product ID is required.")
    private String productId;

    @NotNull(message = "Production quantity is required.")
    @Min(value = 1, message = "Production quantity must be at least 1.")
    private Long productionQuantity;

    @NotNull(message = "Production date is required.")
    private LocalDate productionDate;
}