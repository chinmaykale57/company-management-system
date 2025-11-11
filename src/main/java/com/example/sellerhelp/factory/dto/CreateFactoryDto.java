package com.example.sellerhelp.factory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateFactoryDto {

    @NotBlank(message = "Factory name is required.")
    @Size(min = 3, max = 150, message = "Factory name must be between 3 and 150 characters.")
    private String name;

    @NotBlank(message = "City is required.")
    private String city;

    private String address;

    private String plantHeadUserId;
}