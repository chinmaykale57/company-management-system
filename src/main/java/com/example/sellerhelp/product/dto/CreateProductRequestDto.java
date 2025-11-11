package com.example.sellerhelp.product.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CreateProductRequestDto {

    @NotBlank(message = "Target Factory ID is required.")
    private String factoryId;

    @NotEmpty(message = "Request must contain at least one product.")
    @Valid // Ensures validation rules on ProductRequestItemDto are checked
    private List<ProductRequestItemDto> products;
}