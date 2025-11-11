package com.example.sellerhelp.tool.dto;

import com.example.sellerhelp.constant.Expensive;
import com.example.sellerhelp.constant.Perishable;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ToolDto {
    private String toolId;
    private String name;
    private String categoryName;
    private String imageUrl;
    private Perishable isPerishable;
    private Expensive isExpensive;
    private Long threshold;
    private LocalDateTime createdAt;
}