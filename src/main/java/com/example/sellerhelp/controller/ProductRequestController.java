package com.example.sellerhelp.controller;

import com.example.sellerhelp.appuser.dto.PageableDto;
import com.example.sellerhelp.common.ApiResponseDto;
import com.example.sellerhelp.product.dto.CreateProductRequestDto;
import com.example.sellerhelp.product.dto.ProductRequestDto;
import com.example.sellerhelp.product.service.ProductRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/product-requests")
@RequiredArgsConstructor
public class ProductRequestController {

    private final ProductRequestService productRequestService;

    /**
     * Creates a new product request. Accessible only by CENTRAL_OFFICE users.
     * @param dto The request body containing the factory and product details.
     * @return The newly created product request.
     */
    @PostMapping
    @PreAuthorize("hasRole('CENTRAL_OFFICE')")
    public ResponseEntity<ApiResponseDto<ProductRequestDto>> createProductRequest(@Valid @RequestBody CreateProductRequestDto dto) {
        ProductRequestDto createdRequest = productRequestService.createProductRequest(dto);
        return new ResponseEntity<>(ApiResponseDto.ok(createdRequest, "Product request created successfully."), HttpStatus.CREATED);
    }

    /**
     * Retrieves a paginated list of all product requests in the system. Accessible only by ADMIN.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto<Page<ProductRequestDto>>> getAllProductRequests(@ModelAttribute PageableDto pageableDto) {
        Page<ProductRequestDto> requests = productRequestService.getAllProductRequests(pageableDto);
        return ResponseEntity.ok(ApiResponseDto.ok(requests));
    }

    /**
     * Retrieves a paginated list of product requests for a specific factory.
     * Accessible by ADMIN and PLANT_HEAD.
     */
    @GetMapping("/factory/{factoryId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PLANT_HEAD')")
    public ResponseEntity<ApiResponseDto<Page<ProductRequestDto>>> getProductRequestsByFactory(
            @PathVariable String factoryId,
            @ModelAttribute PageableDto pageableDto) {
        Page<ProductRequestDto> requests = productRequestService.getProductRequestsByFactory(factoryId, pageableDto);
        return ResponseEntity.ok(ApiResponseDto.ok(requests));
    }

    /**
     * Retrieves a single product request by its ID.
     * Accessible by ADMIN, PLANT_HEAD, and CENTRAL_OFFICE.
     */
    @GetMapping("/{requestId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PLANT_HEAD', 'CENTRAL_OFFICE')")
    public ResponseEntity<ApiResponseDto<ProductRequestDto>> getProductRequestById(@PathVariable Long requestId) {
        ProductRequestDto request = productRequestService.getProductRequestById(requestId);
        return ResponseEntity.ok(ApiResponseDto.ok(request));
    }

    /**
     * Approves a product request. Accessible by ADMIN and PLANT_HEAD.
     */
    @PostMapping("/{requestId}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'PLANT_HEAD')")
    public ResponseEntity<ApiResponseDto<ProductRequestDto>> approveRequest(@PathVariable Long requestId) {
        ProductRequestDto approvedRequest = productRequestService.approveRequest(requestId);
        return ResponseEntity.ok(ApiResponseDto.ok(approvedRequest, "Request approved successfully."));
    }

    /**
     * Rejects a product request. Accessible by ADMIN and PLANT_HEAD.
     */
    @PostMapping("/{requestId}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'PLANT_HEAD')")
    public ResponseEntity<ApiResponseDto<ProductRequestDto>> rejectRequest(@PathVariable Long requestId) {
        ProductRequestDto rejectedRequest = productRequestService.rejectRequest(requestId);
        return ResponseEntity.ok(ApiResponseDto.ok(rejectedRequest, "Request rejected successfully."));
    }
}