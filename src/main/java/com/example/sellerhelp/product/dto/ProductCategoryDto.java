package com.example.sellerhelp.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductCategoryDto {
    private Long id;

    @NotBlank(message = "Category name is required.")
    @Size(min = 3, max = 150)
    private String name;

    private String description;
}