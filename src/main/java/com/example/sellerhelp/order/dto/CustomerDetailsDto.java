package com.example.sellerhelp.order.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerDetailsDto {

    @NotBlank(message = "Customer name is required.")
    private String name;

    @NotBlank(message = "Customer email is required.")
    @Email(message = "Please provide a valid customer email address.")
    private String email;

    private String phone;
}