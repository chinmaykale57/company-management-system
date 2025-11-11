package com.example.sellerhelp.appuser.dto;

import com.example.sellerhelp.constant.ActiveStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class DealerDto {
    private String userId;
    private String name;
    private String email;
    private String phone;
    private ActiveStatus status;
    private LocalDateTime createdAt;
    // We can add fields for Total Sales, Reward Points, etc., later
}