package com.example.sellerhelp.tool.dto;

import com.example.sellerhelp.constant.Expensive;
import com.example.sellerhelp.constant.Perishable;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateToolDto {

    @NotBlank(message = "Tool name is required.")
    private String name;

    @NotNull(message = "Category ID is required.")
    private Long categoryId;

    private String imageUrl;

    @NotNull
    private Perishable isPerishable = Perishable.NON_PERISHABLE;

    @NotNull
    private Expensive isExpensive = Expensive.INEXPENSIVE;

    @NotNull
    @Min(0)
    private Long threshold = 0L;
}