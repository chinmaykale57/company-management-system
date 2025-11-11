package com.example.sellerhelp.order.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class DealerInvoiceDto {
    private String invoiceId;
    private String orderId;
    private String dealerId;
    private String dealerName;
    private String pdfUrl;
    private LocalDateTime createdAt;
}