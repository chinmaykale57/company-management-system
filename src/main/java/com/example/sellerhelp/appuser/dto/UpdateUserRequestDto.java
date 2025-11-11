package com.example.sellerhelp.appuser.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserRequestDto {

    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    private String phone;

    private String imageUrl;

    // Only an Owner should be able to change a user's role
    private String roleName;
}