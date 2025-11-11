package com.example.sellerhelp.order.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FulfillOrderDto {

    @NotBlank(message = "The fulfilling factory ID is required.")
    private String factoryId;
}