package com.example.sellerhelp.controller;

import com.example.sellerhelp.appuser.dto.DealerDto;
import com.example.sellerhelp.appuser.dto.LinkedCustomerDto;
import com.example.sellerhelp.appuser.dto.PageableDto;
import com.example.sellerhelp.appuser.service.DealerService;
import com.example.sellerhelp.common.ApiResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dealers")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'CENTRAL_OFFICE_HEAD')") // All methods in this controller require ADMIN role
public class DealerController {

    private final DealerService dealerService;

    /**
     * Retrieves a paginated list of all dealers.
     */
    @GetMapping
    public ResponseEntity<ApiResponseDto<Page<DealerDto>>> getAllDealers(@ModelAttribute PageableDto pageableDto) {
        Page<DealerDto> dealers = dealerService.getAllDealers(pageableDto);
        return ResponseEntity.ok(ApiResponseDto.ok(dealers));
    }

    /**
     * Retrieves a single dealer by their public User ID.
     */
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponseDto<DealerDto>> getDealerById(@PathVariable String userId) {
        DealerDto dealer = dealerService.getDealerById(userId);
        return ResponseEntity.ok(ApiResponseDto.ok(dealer));
    }

    /**
     * Marks a dealer as INACTIVE.
     */
    @PostMapping("/{userId}/suspend")
    public ResponseEntity<ApiResponseDto<Void>> suspendDealer(@PathVariable String userId) {
        dealerService.suspendDealer(userId);
        return ResponseEntity.ok(ApiResponseDto.ok(null, "Dealer has been suspended."));
    }

    /**
     * Marks a dealer as ACTIVE.
     */
    @PostMapping("/{userId}/approve")
    public ResponseEntity<ApiResponseDto<Void>> approveDealer(@PathVariable String userId) {
        dealerService.approveDealer(userId);
        return ResponseEntity.ok(ApiResponseDto.ok(null, "Dealer has been approved/activated."));
    }

    /**
     * Retrieves a paginated list of all customers linked to the currently authenticated dealer.
     * This is a special endpoint that requires the DEALER role, so it's here instead of the admin controller.
     */
    @GetMapping("/my-customers")
    @PreAuthorize("hasRole('DEALER')")
    public ResponseEntity<ApiResponseDto<Page<LinkedCustomerDto>>> getMyLinkedCustomers(@ModelAttribute PageableDto pageableDto) {
        Page<LinkedCustomerDto> customers = dealerService.getMyLinkedCustomers(pageableDto);
        return ResponseEntity.ok(ApiResponseDto.ok(customers));
    }
}