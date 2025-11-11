package com.example.sellerhelp.tool.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AssignToolToBucketDto {
    @NotBlank
    private String toolId;
    @NotBlank
    private String bucketId;
    @NotNull @Min(1)
    private Long quantity;
}