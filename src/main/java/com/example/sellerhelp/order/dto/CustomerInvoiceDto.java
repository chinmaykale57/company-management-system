package com.example.sellerhelp.order.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CustomerInvoiceDto {
    private String invoiceId;
    private String orderId;
    private String customerId;
    private String customerName;
    private String dealerId;
    private String dealerName;
    private LocalDateTime createdAt;
}