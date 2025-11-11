package com.example.sellerhelp.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CreateCustomerSaleDto {

    @NotNull(message = "Customer details are required.")
    @Valid // Ensures the nested CustomerDetailsDto is validated
    private CustomerDetailsDto customer;

    @NotEmpty(message = "A sale must contain at least one product.")
    @Valid // Ensures the items in the list are validated
    private List<OrderItemDto> products; // We can reuse the OrderItemDto
}
