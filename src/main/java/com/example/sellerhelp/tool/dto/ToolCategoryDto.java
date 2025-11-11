package com.example.sellerhelp.tool.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ToolCategoryDto {
    private Long id;

    @NotBlank(message = "Category name is required.")
    @Size(min = 3, max = 150)
    private String name;

    private String description;
}