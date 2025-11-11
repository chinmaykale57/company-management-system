package com.example.sellerhelp.factory.dto;

import com.example.sellerhelp.constant.ActiveStatus;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateFactoryDto {

    @Size(min = 3, max = 150)
    private String name;
    private String city;
    private String address;
    private String plantHeadUserId;
    private ActiveStatus status; // Allow Owner to activate/deactivate
}