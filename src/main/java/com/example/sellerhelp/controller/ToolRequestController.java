package com.example.sellerhelp.controller;

import com.example.sellerhelp.common.ApiResponseDto;
import com.example.sellerhelp.tool.dto.CreateToolRequestDto;
import com.example.sellerhelp.tool.dto.ToolIssuanceDto;
import com.example.sellerhelp.tool.dto.ToolRequestDto;
import com.example.sellerhelp.tool.service.ToolRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tool-requests")
@RequiredArgsConstructor
public class ToolRequestController {

    private final ToolRequestService toolRequestService;

    @PostMapping
    @PreAuthorize("hasAnyRole('WORKER', 'CHIEF_SUPERVISOR', 'PLANT_HEAD', 'ADMIN')")
    public ResponseEntity<ApiResponseDto<ToolRequestDto>> createRequest(@Valid @RequestBody CreateToolRequestDto dto) {
        ToolRequestDto createdRequest = toolRequestService.createToolRequest(dto);
        return new ResponseEntity<>(ApiResponseDto.ok(createdRequest, "Tool request created successfully."), HttpStatus.CREATED);
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('CHIEF_SUPERVISOR', 'PLANT_HEAD', 'ADMIN')")
    public ResponseEntity<ApiResponseDto<List<ToolIssuanceDto>>> approveRequest(@PathVariable Long id) {
        List<ToolIssuanceDto> issuances = toolRequestService.approveAndIssueToolRequest(id);
        return ResponseEntity.ok(ApiResponseDto.ok(issuances, "Request approved and tools issued successfully."));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('CHIEF_SUPERVISOR', 'PLANT_HEAD', 'ADMIN')")
    public ResponseEntity<ApiResponseDto<Void>> rejectRequest(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String comment = body.get("comment");
        if (comment == null || comment.isBlank()) {
            throw new IllegalArgumentException("A comment is required when rejecting a request.");
        }
        toolRequestService.rejectToolRequest(id, comment);
        return ResponseEntity.ok(ApiResponseDto.ok(null, "Request rejected successfully."));
    }
}