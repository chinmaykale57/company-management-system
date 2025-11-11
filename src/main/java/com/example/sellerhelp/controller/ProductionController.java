package com.example.sellerhelp.controller;

import com.example.sellerhelp.appuser.dto.PageableDto;
import com.example.sellerhelp.common.ApiResponseDto;
import com.example.sellerhelp.product.dto.FactoryProductionDto;
import com.example.sellerhelp.product.dto.ProductStockDto;
import com.example.sellerhelp.product.dto.RecordProductionDto;
import com.example.sellerhelp.product.service.ProductionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ProductionController {

    private final ProductionService productionService;

    /**
     * Records a new production entry for the Plant Head's own factory.
     * Accessible by PLANT_HEAD and CHIEF_SUPERVISOR as per SRS.
     */
    @PostMapping("/production")
    @PreAuthorize("hasAnyRole('PLANT_HEAD', 'CHIEF_SUPERVISOR')")
    public ResponseEntity<ApiResponseDto<FactoryProductionDto>> recordProduction(@Valid @RequestBody RecordProductionDto dto) {
        FactoryProductionDto recordedProduction = productionService.recordProduction(dto);
        return new ResponseEntity<>(ApiResponseDto.ok(recordedProduction, "Production recorded successfully."), HttpStatus.CREATED);
    }

    /**
     * Gets a paginated list of all product stock for a specific factory.
     * Accessible by ADMIN and PLANT_HEAD.
     */
    @GetMapping("/factories/{factoryId}/stock")
    @PreAuthorize("hasAnyRole('ADMIN', 'PLANT_HEAD')")
    public ResponseEntity<ApiResponseDto<Page<ProductStockDto>>> getStockByFactory(
            @PathVariable String factoryId,
            @ModelAttribute PageableDto pageableDto) {
        Page<ProductStockDto> stockPage = productionService.getStockByFactory(factoryId, pageableDto);
        return ResponseEntity.ok(ApiResponseDto.ok(stockPage));
    }

    /**
     * Gets a paginated list of historical production records for a specific factory.
     * Accessible by ADMIN and PLANT_HEAD.
     */
    @GetMapping("/factories/{factoryId}/production-records")
    @PreAuthorize("hasAnyRole('ADMIN', 'PLANT_HEAD')")
    public ResponseEntity<ApiResponseDto<Page<FactoryProductionDto>>> getProductionRecordsByFactory(
            @PathVariable String factoryId,
            @ModelAttribute PageableDto pageableDto) {
        Page<FactoryProductionDto> recordsPage = productionService.getProductionRecordsByFactory(factoryId, pageableDto);
        return ResponseEntity.ok(ApiResponseDto.ok(recordsPage));
    }

    /**
     * Gets a consolidated list of stock levels for a single product across all factories.
     * Crucial for Central Office fulfillment decisions.
     * Accessible by ADMIN and CENTRAL_OFFICE users.
     */
    @GetMapping("/products/{productId}/stock-levels")
    @PreAuthorize("hasAnyRole('ADMIN', 'CENTRAL_OFFICE')")
    public ResponseEntity<ApiResponseDto<Page<ProductStockDto>>> getStockLevelsForProduct(@PathVariable String productId,
    @ModelAttribute PageableDto pageableDto) {
        Page<ProductStockDto> stockLevels = productionService.getStockLevelsForProduct(productId, pageableDto);
        return ResponseEntity.ok(ApiResponseDto.ok(stockLevels));
    }
}