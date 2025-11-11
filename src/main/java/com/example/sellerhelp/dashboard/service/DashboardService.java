package com.example.sellerhelp.dashboard.service;

import com.example.sellerhelp.constant.DealerOrderStatus;
import com.example.sellerhelp.constant.ProductRequestStatus;
import com.example.sellerhelp.dashboard.dto.CentralOfficeDashboardDto;
import com.example.sellerhelp.dashboard.dto.ProductIdSalesDto;
import com.example.sellerhelp.dashboard.dto.ProductSalesDataDto;
import com.example.sellerhelp.order.repository.DealerInvoiceRepository;
import com.example.sellerhelp.order.repository.DealerOrderMappingRepository;
import com.example.sellerhelp.order.repository.DealerOrderRepository;
import com.example.sellerhelp.product.entity.Product;
import com.example.sellerhelp.product.repository.ProductRepository;
import com.example.sellerhelp.product.repository.ProductRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final DealerOrderRepository dealerOrderRepository;
    private final DealerOrderMappingRepository dealerOrderMappingRepository;
    private final DealerInvoiceRepository dealerInvoiceRepository;
    private final ProductRequestRepository productRequestRepository;
    private final ProductRepository productRepository;

    public CentralOfficeDashboardDto getDashboardData() {
        long pendingDealerOrders = dealerOrderRepository.countByStatus(DealerOrderStatus.PENDING);
        long pendingProductRequests = productRequestRepository.countByStatus(ProductRequestStatus.REQUESTED);
        long totalInvoices = dealerInvoiceRepository.count();

        LocalDate today = LocalDate.now();
        BigDecimal salesToday = dealerOrderRepository.sumTotalPriceByStatusAndDate(
                DealerOrderStatus.APPROVED, today.atStartOfDay());
        BigDecimal salesThisWeek = dealerOrderRepository.sumTotalPriceByStatusAndDate(
                DealerOrderStatus.APPROVED, today.with(DayOfWeek.MONDAY).atStartOfDay());
        BigDecimal salesThisMonth = dealerOrderRepository.sumTotalPriceByStatusAndDate(
                DealerOrderStatus.APPROVED, today.withDayOfMonth(1).atStartOfDay());

        List<ProductSalesDataDto> fastMovingProducts = getProductSalesData(5, "fast");
        List<ProductSalesDataDto> slowMovingProducts = getProductSalesData(5, "slow"); // <-- CALL FOR SLOW-MOVING

        return CentralOfficeDashboardDto.builder()
                .pendingDealerOrders(pendingDealerOrders)
                .pendingProductRequests(pendingProductRequests)
                .totalInvoicesGenerated(totalInvoices)
                .salesToday(salesToday)
                .salesThisWeek(salesThisWeek)
                .salesThisMonth(salesThisMonth)
                .fastMovingProducts(fastMovingProducts)
                .slowMovingProducts(slowMovingProducts) // <-- ADD TO BUILDER
                .build();
    }

    private List<ProductSalesDataDto> getProductSalesData(int limit, String type) {
        List<ProductIdSalesDto> productIdsAndSales;

        if ("fast".equalsIgnoreCase(type)) {
            productIdsAndSales = dealerOrderMappingRepository.findFastMovingProductIds(PageRequest.of(0, limit));
        } else {
            productIdsAndSales = dealerOrderMappingRepository.findSlowMovingProductIds(PageRequest.of(0, limit));
        }

        if (productIdsAndSales.isEmpty()) {
            return List.of();
        }

        List<Long> productIds = productIdsAndSales.stream()
                .map(ProductIdSalesDto::getProductId)
                .toList();

        Map<Long, String> productNames = productRepository.findAllById(productIds).stream()
                .collect(Collectors.toMap(Product::getId, Product::getName));

        return productIdsAndSales.stream()
                .map(idDto -> new ProductSalesDataDto(
                        productNames.getOrDefault(idDto.getProductId(), "Unknown Product"),
                        idDto.getQuantitySold()
                ))
                .toList();
    }
}