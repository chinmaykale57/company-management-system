package com.example.sellerhelp.tool.dto;

import com.example.sellerhelp.constant.ToolNature;
import com.example.sellerhelp.constant.ToolRequestStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ToolRequestDto {
    private Long id;
    private String requestNumber;
    private String workerName;
    private String factoryId;
    private ToolNature nature;
    private ToolRequestStatus status;
    private String comment;
    private LocalDateTime createdAt;
    private List<ToolRequestItemDto> tools;
}