package com.example.sellerhelp.appuser.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class LinkedCustomerDto {
    private String userId;
    private String name;
    private String email;
    private String phone;
    private LocalDateTime firstPurchaseDate; // The date this mapping was created
}