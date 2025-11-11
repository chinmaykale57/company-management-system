package com.example.sellerhelp.controller;

import com.example.sellerhelp.appuser.dto.PageableDto;
import com.example.sellerhelp.common.ApiResponseDto;
import com.example.sellerhelp.tool.dto.*;
import com.example.sellerhelp.tool.service.ToolService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tools")
@RequiredArgsConstructor
public class ToolController {

    private final ToolService toolService;

    // --- CATEGORY MANAGEMENT (ADMIN, PLANT_HEAD) ---

    @PostMapping("/categories")
    @PreAuthorize("hasAnyRole('ADMIN', 'PLANT_HEAD')")
    public ResponseEntity<ApiResponseDto<ToolCategoryDto>> createToolCategory(@Valid @RequestBody ToolCategoryDto dto) {
        ToolCategoryDto createdCategory = toolService.createToolCategory(dto);
        return new ResponseEntity<>(ApiResponseDto.ok(createdCategory, "Tool category created successfully."), HttpStatus.CREATED);
    }

    @GetMapping("/categories")
    @PreAuthorize("isAuthenticated()") // Any authenticated user can see the categories
    public ResponseEntity<ApiResponseDto<List<ToolCategoryDto>>> getAllToolCategories() {
        List<ToolCategoryDto> categories = toolService.getAllToolCategories();
        return ResponseEntity.ok(ApiResponseDto.ok(categories));
    }

    // --- MASTER TOOL MANAGEMENT (ADMIN, PLANT_HEAD) ---

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PLANT_HEAD')")
    public ResponseEntity<ApiResponseDto<ToolDto>> createTool(@Valid @RequestBody CreateToolDto dto) {
        ToolDto createdTool = toolService.createTool(dto);
        return new ResponseEntity<>(ApiResponseDto.ok(createdTool, "Master tool created successfully."), HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()") // Any authenticated user can see the master list of tools
    public ResponseEntity<ApiResponseDto<Page<ToolDto>>> getAllMasterTools(@ModelAttribute PageableDto pageableDto) {
        Page<ToolDto> tools = toolService.getAllMasterTools(pageableDto);
        return ResponseEntity.ok(ApiResponseDto.ok(tools));
    }

    // --- FACTORY STOCK MANAGEMENT (PLANT_HEAD) ---

    @PostMapping("/stock/factory/{factoryId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PLANT_HEAD')")
    public ResponseEntity<ApiResponseDto<ToolStockDto>> addStockToFactory(
            @PathVariable String factoryId,
            @Valid @RequestBody AddToolStockDto dto) {
        ToolStockDto updatedStock = toolService.addStockToFactory(factoryId, dto);
        return ResponseEntity.ok(ApiResponseDto.ok(updatedStock, "Stock added to factory successfully."));
    }



    // --- TOOL RETURN & CONFISCATION (CHIEF_SUPERVISOR) ---

    @PostMapping("/issuance/{issuanceId}/return")
    @PreAuthorize("hasAnyRole('CHIEF_SUPERVISOR', 'PLANT_HEAD', 'ADMIN')")
    public ResponseEntity<ApiResponseDto<Void>> returnTool(
            @PathVariable Long issuanceId,
            @RequestBody Map<String, Long> body) {
        Long fitQuantity = body.get("fitQuantity");
        Long unfitQuantity = body.get("unfitQuantity");
        if (fitQuantity == null || unfitQuantity == null) {
            throw new IllegalArgumentException("Both 'fitQuantity' and 'unfitQuantity' are required.");
        }
        toolService.returnTool(issuanceId, fitQuantity, unfitQuantity);
        return ResponseEntity.ok(ApiResponseDto.ok(null, "Tool returned successfully."));
    }

    @PostMapping("/issuance/{issuanceId}/confiscate")
    @PreAuthorize("hasAnyRole('CHIEF_SUPERVISOR', 'PLANT_HEAD', 'ADMIN')")
    public ResponseEntity<ApiResponseDto<Void>> confiscateTool(@PathVariable Long issuanceId) {
        toolService.confiscateTool(issuanceId);
        return ResponseEntity.ok(ApiResponseDto.ok(null, "Tool marked as confiscated successfully."));
    }

    // --- WORKER-CENTRIC VIEWS ---

    @GetMapping("/worker/{workerId}/issued")
    @PreAuthorize("isAuthenticated()") // A supervisor or the worker themselves can view this
    public ResponseEntity<ApiResponseDto<Page<ToolIssuanceDto>>> getToolsByWorker(
            @PathVariable String workerId,
            @ModelAttribute PageableDto pageableDto) {
        Page<ToolIssuanceDto> issuedTools = toolService.getToolsByWorker(workerId, pageableDto);
        return ResponseEntity.ok(ApiResponseDto.ok(issuedTools));
    }
}