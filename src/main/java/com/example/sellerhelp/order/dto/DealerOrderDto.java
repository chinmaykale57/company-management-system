package com.example.sellerhelp.order.dto;

import com.example.sellerhelp.constant.DealerOrderStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class DealerOrderDto {
    private String orderId;
    private String dealerName;
    private String dealerId;
    private BigDecimal totalPrice;
    private DealerOrderStatus status;
    private String comment;
    private LocalDateTime createdAt;
    private List<OrderItemDto> products;
}