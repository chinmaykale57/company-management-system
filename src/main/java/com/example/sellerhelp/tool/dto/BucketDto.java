package com.example.sellerhelp.tool.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BucketDto {
    private Long id;

    @NotBlank(message = "Bucket ID is required")
    private String bucketId; // e.g., A-01-C-03

    private String stackNo;
    private String col;
    private String row;

    @NotBlank(message = "Factory ID is required")
    private String factoryId;
}
