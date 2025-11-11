package com.example.sellerhelp.controller;

import com.example.sellerhelp.appuser.dto.PageableDto;
import com.example.sellerhelp.common.ApiResponseDto;
import com.example.sellerhelp.tool.dto.ApproveExtensionDto;
import com.example.sellerhelp.tool.dto.ToolIssuanceDto;
import com.example.sellerhelp.tool.service.ToolService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tool-issuances")
@RequiredArgsConstructor
public class ToolIssuanceController {

    private final ToolService toolService;

    /**
     * Endpoint for a WORKER to request an extension on a borrowed tool.
     */
    @PostMapping("/{issuanceId}/request-extension")
    @PreAuthorize("hasRole('WORKER')")
    public ResponseEntity<ApiResponseDto<Void>> requestExtension(@PathVariable Long issuanceId) {
        toolService.requestExtension(issuanceId);
        return ResponseEntity.ok(ApiResponseDto.ok(null, "Extension requested successfully. Awaiting supervisor approval."));
    }

    /**
     * Endpoint for a CHIEF_SUPERVISOR or PLANT_HEAD to approve or deny an extension request.
     */
    @PostMapping("/{issuanceId}/process-extension")
    @PreAuthorize("hasAnyRole('CHIEF_SUPERVISOR', 'PLANT_HEAD')")
    public ResponseEntity<ApiResponseDto<ToolIssuanceDto>> processExtension(
            @PathVariable Long issuanceId,
            @Valid @RequestBody ApproveExtensionDto dto) {
        ToolIssuanceDto updatedIssuance = toolService.processExtensionRequest(issuanceId, dto);
        String message = dto.getApproved() ? "Extension approved successfully." : "Extension denied.";
        return ResponseEntity.ok(ApiResponseDto.ok(updatedIssuance, message));
    }

    /**
     * Endpoint for a CHIEF_SUPERVISOR or PLANT_HEAD to get a list of all overdue tools
     * in their factory.
     */
    @GetMapping("/overdue")
    @PreAuthorize("hasAnyRole('CHIEF_SUPERVISOR', 'PLANT_HEAD')")
    public ResponseEntity<ApiResponseDto<Page<ToolIssuanceDto>>> getOverdueTools(@ModelAttribute PageableDto pageableDto) {
        Page<ToolIssuanceDto> overdueTools = toolService.getOverdueTools(pageableDto);
        return ResponseEntity.ok(ApiResponseDto.ok(overdueTools));
    }
}