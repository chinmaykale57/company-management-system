package com.example.sellerhelp.controller;

import com.example.sellerhelp.appuser.dto.PageableDto;
import com.example.sellerhelp.common.ApiResponseDto;
import com.example.sellerhelp.tool.dto.ToolIssuanceDto;
import com.example.sellerhelp.tool.service.ToolService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/worker") // A dedicated namespace for worker-specific actions
@RequiredArgsConstructor
public class WorkerController {

    private final ToolService toolService;

    /**
     * Retrieves a paginated list of tools currently issued to the logged-in worker.
     */
    @GetMapping("/my-tools")
    @PreAuthorize("hasRole('WORKER')")
    public ResponseEntity<ApiResponseDto<Page<ToolIssuanceDto>>> getMyTools(@ModelAttribute PageableDto pageableDto) {
        Page<ToolIssuanceDto> myTools = toolService.getMyIssuedTools(pageableDto);
        return ResponseEntity.ok(ApiResponseDto.ok(myTools));
    }

    /**
     * Allows the logged-in worker to signal their intent to return a tool.
     * This changes the issuance status to RETURN_PENDING for a supervisor to process.
     */
    @PostMapping("/my-tools/{issuanceId}/initiate-return")
    @PreAuthorize("hasRole('WORKER')")
    public ResponseEntity<ApiResponseDto<Void>> initiateReturn(@PathVariable Long issuanceId) {
        toolService.initiateReturn(issuanceId);
        return ResponseEntity.ok(ApiResponseDto.ok(null, "Return initiated. Please hand the tool to your supervisor for processing."));
    }
}