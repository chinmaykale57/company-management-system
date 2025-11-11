package com.example.sellerhelp.controller;

import com.example.sellerhelp.common.ApiResponseDto;
import com.example.sellerhelp.order.dto.CreateCustomerSaleDto;
import com.example.sellerhelp.order.dto.CustomerInvoiceDto;
import com.example.sellerhelp.order.service.CustomerSaleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sales/customer")
@RequiredArgsConstructor
public class CustomerSaleController {

    private final CustomerSaleService customerSaleService;

    /**
     * Records a new sale to a customer. This is a single, atomic action that:
     * 1. Finds or creates the customer user account.
     * 2. Links the customer to the currently authenticated dealer.
     * 3. Validates and decrements the dealer's own stock.
     * 4. Creates a CustomerOrder and CustomerInvoice.
     * 5. Sends a welcome email to a new customer.
     *
     * Accessible only by users with the DEALER role.
     * @param dto The request body containing customer details and the products sold.
     * @return A DTO of the newly generated customer invoice.
     */
    @PostMapping
    @PreAuthorize("hasRole('DEALER')")
    public ResponseEntity<ApiResponseDto<CustomerInvoiceDto>> createCustomerSale(@Valid @RequestBody CreateCustomerSaleDto dto) {
        CustomerInvoiceDto invoiceDto = customerSaleService.createCustomerSale(dto);
        return new ResponseEntity<>(ApiResponseDto.ok(invoiceDto, "Customer sale recorded and invoice generated successfully."), HttpStatus.CREATED);
    }
}