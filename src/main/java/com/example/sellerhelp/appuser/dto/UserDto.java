package com.example.sellerhelp.appuser.dto;

import com.example.sellerhelp.constant.ActiveStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;


import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDto {
    private String userId;
    private String name;
    private String email;
    private String phone;
    private String imageUrl;
    private String roleName;
    private List<String> factories;
    private String bay;
    private ActiveStatus isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
