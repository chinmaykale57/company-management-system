package com.example.sellerhelp.factory.dto;

import com.example.sellerhelp.constant.ActiveStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FactoryDetailsDto {
    private String factoryId;
    private String name;
    private String city;
    private String address;
    private String plantHeadName;
    private String plantHeadUserId;
    private ActiveStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}