package com.example.sellerhelp.tool.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ToolStockDto {
    private String toolId;
    private String toolName;
    private String factoryId;
    private Long totalQuantity;
    private Long availableQuantity;
    private Long issuedQuantity;
    private LocalDateTime lastUpdatedAt;
}