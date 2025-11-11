package com.example.sellerhelp.dashboard.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class CentralOfficeDashboardDto {
    private long pendingDealerOrders;
    private long pendingProductRequests;
    private long totalInvoicesGenerated;
    private BigDecimal salesToday;
    private BigDecimal salesThisWeek;
    private BigDecimal salesThisMonth;
    private List<ProductSalesDataDto> fastMovingProducts;
    private List<ProductSalesDataDto> slowMovingProducts;
}