package com.example.sellerhelp.appuser.dto;

import com.example.sellerhelp.constant.ActiveStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// UserFilterDto.java
@Getter
@Setter
@NoArgsConstructor
public class UserFilterDto {
    private String name;
    private String email;
    private String phone;
    private String roleId;
    private String factoryId;
    private String bayId;
    private ActiveStatus status;
}


