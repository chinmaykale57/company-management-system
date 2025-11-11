package com.example.sellerhelp.controller;

import com.example.sellerhelp.appuser.dto.PageableDto;
import com.example.sellerhelp.common.ApiResponseDto;
import com.example.sellerhelp.order.dto.CreateDealerOrderDto;
import com.example.sellerhelp.order.dto.DealerInvoiceDto;
import com.example.sellerhelp.order.dto.DealerOrderDto;
import com.example.sellerhelp.order.dto.FulfillOrderDto;
import com.example.sellerhelp.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders/dealer")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;


    /**
     * Creates a new dealer order. Accessible only by users with the DEALER role.
     * The dealer's identity is taken from the authentication token.
     * @param dto The request body containing the list of products to order.
     * @return The newly created dealer order with PENDING status.
     */
    @PostMapping
    @PreAuthorize("hasRole('DEALER')")
    public ResponseEntity<ApiResponseDto<DealerOrderDto>> createDealerOrder(@Valid @RequestBody CreateDealerOrderDto dto) {
        DealerOrderDto createdOrder = orderService.createDealerOrder(dto);
        return new ResponseEntity<>(ApiResponseDto.ok(createdOrder, "Order placed successfully."), HttpStatus.CREATED);
    }

    /**
     * Fulfills a pending dealer order. Accessible only by CENTRAL_OFFICE users.
     * This action checks stock, updates inventory, and generates an invoice.
     */
    @PostMapping("/{orderId}/fulfill")
    @PreAuthorize("hasRole('CENTRAL_OFFICE')")
    public ResponseEntity<ApiResponseDto<DealerInvoiceDto>> fulfillDealerOrder(
            @PathVariable String orderId,
            @Valid @RequestBody FulfillOrderDto dto) {
        DealerInvoiceDto invoiceDto = orderService.fulfillDealerOrder(orderId, dto);
        return ResponseEntity.ok(ApiResponseDto.ok(invoiceDto, "Order fulfilled and invoice generated successfully."));
    }

    /**
     * Retrieves a paginated list of all dealer orders. For administrative review.
     * Accessible by ADMIN and CENTRAL_OFFICE users.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CENTRAL_OFFICE')")
    public ResponseEntity<ApiResponseDto<Page<DealerOrderDto>>> getAllDealerOrders(@ModelAttribute PageableDto pageableDto) {
        Page<DealerOrderDto> orders = orderService.getAllDealerOrders(pageableDto);
        return ResponseEntity.ok(ApiResponseDto.ok(orders));
    }


    /**
     * Retrieves a single dealer order by its public ID.
     * Accessible by ADMIN and CENTRAL_OFFICE. A Dealer should have a separate "my-orders" endpoint.
     */
    @GetMapping("/{orderId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CENTRAL_OFFICE')")
    public ResponseEntity<ApiResponseDto<DealerOrderDto>> getDealerOrderById(@PathVariable String orderId) {
        DealerOrderDto order = orderService.getDealerOrderById(orderId);
        return ResponseEntity.ok(ApiResponseDto.ok(order));
    }
}