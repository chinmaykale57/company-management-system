package com.example.sellerhelp.order.service;

import com.example.sellerhelp.appuser.dto.PageableDto;
import com.example.sellerhelp.appuser.entity.User;
import com.example.sellerhelp.constant.UserRole;
import com.example.sellerhelp.appuser.repository.UserRepository;
import com.example.sellerhelp.constant.DealerOrderStatus;
import com.example.sellerhelp.exception.ConflictException;
import com.example.sellerhelp.exception.ResourceNotFoundException;
import com.example.sellerhelp.factory.entity.Factory;
import com.example.sellerhelp.factory.repository.FactoryRepository;
import com.example.sellerhelp.notification.service.EmailService;
import com.example.sellerhelp.order.dto.*;
import com.example.sellerhelp.order.entity.DealerInvoice;
import com.example.sellerhelp.order.entity.DealerOrder;
import com.example.sellerhelp.order.entity.DealerOrderMapping;
import com.example.sellerhelp.order.entity.DealerStock;
import com.example.sellerhelp.order.repository.DealerInvoiceRepository;
import com.example.sellerhelp.order.repository.DealerOrderRepository;
import com.example.sellerhelp.order.repository.DealerStockRepository;
import com.example.sellerhelp.product.entity.Product;
import com.example.sellerhelp.product.entity.ProductStock;
import com.example.sellerhelp.product.repository.ProductRepository;
import com.example.sellerhelp.product.repository.ProductStockRepository;
import com.example.sellerhelp.security.SecurityService;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final DealerOrderRepository dealerOrderRepository;
    private final DealerInvoiceRepository dealerInvoiceRepository;
    private final DealerStockRepository dealerStockRepository;
    private final ProductRepository productRepository;
    private final ProductStockRepository productStockRepository;
    private final SecurityService securityService;
    private final EntityManager entityManager;
    private final FactoryRepository factoryRepository;
    private final EmailService emailService;
    private final UserRepository userRepository;

    @Transactional
    public DealerOrderDto createDealerOrder(CreateDealerOrderDto dto) {
        User dealer = securityService.getCurrentUser();

        DealerOrder order = DealerOrder.builder()
                .dealer(dealer)
                .status(DealerOrderStatus.PENDING)
                .comment(dto.getComment())
                .build();

        BigDecimal totalPrice = BigDecimal.ZERO;
        List<DealerOrderMapping> mappings = new ArrayList<>();

        for (OrderItemDto item : dto.getProducts()) {
            Product product = productRepository.findByProductId(item.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + item.getProductId()));

            BigDecimal quantityBigDecimal = new BigDecimal(item.getQuantity());
            BigDecimal itemPrice = product.getUnitPrice().multiply(quantityBigDecimal);
            totalPrice = totalPrice.add(itemPrice);

            DealerOrderMapping mapping = DealerOrderMapping.builder()
                    .order(order)
                    .product(product)
                    .quantity(item.getQuantity())
                    .unitPrice(product.getUnitPrice())
                    .build();
            mappings.add(mapping);
        }

        order.setTotalPrice(totalPrice);
        order.setItems(mappings);

        DealerOrder savedOrder = dealerOrderRepository.saveAndFlush(order);
        entityManager.refresh(savedOrder);

        String subjectToDealer = "Your Order has been Received: " + savedOrder.getOrderId();
        String bodyToDealer = String.format(
                "<p>Hello %s,</p>" +
                        "<p>Thank you for your business. We have successfully received your order <b>%s</b>.</p>" +
                        "<p>You will be notified again once the order has been fulfilled by our team.</p>",
                dealer.getName(),
                savedOrder.getOrderId()
        );
        emailService.sendEmail(dealer.getEmail(), subjectToDealer, bodyToDealer);

        Page<User> centralOfficeStaffPage = userRepository.findByRole_Name(UserRole.CENTRAL_OFFICE_HEAD, Pageable.unpaged());
        List<User> centralOfficeStaff = centralOfficeStaffPage.getContent();
        String subjectToCO = "New Dealer Order Received: " + savedOrder.getOrderId();
        String bodyToCO = String.format(
                "<p>A new order (<b>%s</b>) has been placed by dealer <b>%s (%s)</b>.</p>" +
                        "<p>Please log in to the SellerHelp application to view the details and fulfill the order.</p>",
                savedOrder.getOrderId(),
                dealer.getName(),
                dealer.getEmail()
        );
        for (User staff : centralOfficeStaff) {
            emailService.sendEmail(staff.getEmail(), subjectToCO, bodyToCO);
        }

        return toDto(savedOrder);
    }

    @Transactional
    public DealerInvoiceDto fulfillDealerOrder(String orderId, FulfillOrderDto dto) {
        User centralOfficer = securityService.getCurrentUser();
        DealerOrder order = dealerOrderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Dealer Order not found with ID: " + orderId));

        if (order.getStatus() != DealerOrderStatus.PENDING) {
            throw new ConflictException("This order has already been processed. Current status: " + order.getStatus());
        }

        Factory fulfillingFactory = factoryRepository.findByFactoryId(dto.getFactoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Fulfilling factory not found with ID: " + dto.getFactoryId()));

        User dealer = order.getDealer();

        for (DealerOrderMapping mapping : order.getItems()) {
            Product product = mapping.getProduct();
            Long requiredQuantity = mapping.getQuantity();

            // Step 1: Decrement the Factory's stock (existing logic)
            ProductStock factoryStock = productStockRepository.findByFactoryAndProduct(fulfillingFactory, product)
                    .orElseThrow(() -> new ConflictException("Factory '" + fulfillingFactory.getName() + "' has no stock record for product: " + product.getName()));

            if (factoryStock.getQuantity() < requiredQuantity) {
                throw new ConflictException("Insufficient stock at factory '" + fulfillingFactory.getName() + "' for product '" + product.getName() + "'. Required: " + requiredQuantity + ", Available: " + factoryStock.getQuantity());
            }
            factoryStock.setQuantity(factoryStock.getQuantity() - requiredQuantity);
            productStockRepository.save(factoryStock);

            // --- NEW LOGIC: Step 2: Increment the Dealer's stock ---
            DealerStock dealerStock = dealerStockRepository.findByDealerAndProduct(dealer, product)
                    .orElseGet(() -> DealerStock.builder()
                            .dealer(dealer)
                            .product(product)
                            .build());

            dealerStock.setQuantity(dealerStock.getQuantity() + requiredQuantity);
            dealerStockRepository.save(dealerStock);
        }

        order.setStatus(DealerOrderStatus.APPROVED);
        order.setUpdatedBy(centralOfficer);
        dealerOrderRepository.save(order);

        DealerInvoice invoice = DealerInvoice.builder()
                .dealer(dealer)
                .order(order)
                .url("/invoices/dealer/" + order.getOrderId() + ".pdf")
                .build();

        DealerInvoice savedInvoice = dealerInvoiceRepository.saveAndFlush(invoice);
        entityManager.refresh(savedInvoice);

        // Send notification to the dealer
        String subject = "Your Order has been Fulfilled: " + order.getOrderId();
        String body = String.format(
                "<p>Hello %s,</p>" +
                        "<p>Great news! Your order <b>%s</b> has been fulfilled and the products have been added to your inventory.</p>" +
                        "<p>Your invoice (<b>%s</b>) has been generated.</p>",
                dealer.getName(),
                order.getOrderId(),
                savedInvoice.getInvoiceId()
        );
        emailService.sendEmail(dealer.getEmail(), subject, body);

        return toDealerInvoiceDto(savedInvoice);
    }

    /**
     * Retrieves a paginated list of all dealer orders.
     * Can be enhanced later with filtering by status, dealer, etc.
     */
    public Page<DealerOrderDto> getAllDealerOrders(PageableDto pageReq) {
        Pageable pageable = PageRequest.of(pageReq.getPage(), pageReq.getSize(), Sort.by("createdAt").descending());
        Page<DealerOrder> orderPage = dealerOrderRepository.findAll(pageable);
        return orderPage.map(this::toDto);
    }

    /**
     * Retrieves a single dealer order by its public order ID.
     */
    public DealerOrderDto getDealerOrderById(String orderId) {
        return dealerOrderRepository.findByOrderId(orderId)
                .map(this::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Dealer Order not found with ID: " + orderId));
    }

    /**
     * Converts a DealerInvoice entity to its corresponding DTO.
     */
    public DealerInvoiceDto toDealerInvoiceDto(DealerInvoice invoice) {
        return DealerInvoiceDto.builder()
                .invoiceId(invoice.getInvoiceId())
                .orderId(invoice.getOrder() != null ? invoice.getOrder().getOrderId() : null)
                .dealerId(invoice.getDealer().getUserId())
                .dealerName(invoice.getDealer().getName())
                .pdfUrl(invoice.getUrl())
                .createdAt(invoice.getCreatedAt())
                .build();
    }

    // --- DTO CONVERTER ---
    private DealerOrderDto toDto(DealerOrder order) {
        List<OrderItemDto> items = order.getItems().stream()
                .map(mapping -> {
                    OrderItemDto itemDto = new OrderItemDto();
                    itemDto.setProductId(mapping.getProduct().getProductId());
                    itemDto.setQuantity(mapping.getQuantity());
                    return itemDto;
                }).toList();

        return DealerOrderDto.builder()
                .orderId(order.getOrderId())
                .dealerId(order.getDealer().getUserId())
                .dealerName(order.getDealer().getName())
                .totalPrice(order.getTotalPrice())
                .status(order.getStatus())
                .comment(order.getComment())
                .createdAt(order.getCreatedAt())
                .products(items)
                .build();
    }

}