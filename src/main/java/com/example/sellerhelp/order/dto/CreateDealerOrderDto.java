package com.example.sellerhelp.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CreateDealerOrderDto {

    @NotEmpty(message = "An order must contain at least one product.")
    @Valid // Ensures the items in the list are validated
    private List<OrderItemDto> products;

    private String comment; // Optional comment from the dealer
}