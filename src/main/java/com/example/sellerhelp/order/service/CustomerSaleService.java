package com.example.sellerhelp.order.service;

import com.example.sellerhelp.appuser.entity.Role;
import com.example.sellerhelp.appuser.entity.User;
import com.example.sellerhelp.appuser.repository.RoleRepository;
import com.example.sellerhelp.appuser.repository.UserRepository;
import com.example.sellerhelp.constant.UserRole;
import com.example.sellerhelp.exception.ConflictException;
import com.example.sellerhelp.exception.ResourceNotFoundException;
import com.example.sellerhelp.notification.service.EmailService;
import com.example.sellerhelp.order.dto.CreateCustomerSaleDto;
import com.example.sellerhelp.order.dto.CustomerDetailsDto;
import com.example.sellerhelp.order.dto.CustomerInvoiceDto;
import com.example.sellerhelp.order.dto.OrderItemDto;
import com.example.sellerhelp.order.entity.*;
import com.example.sellerhelp.order.repository.*;
import com.example.sellerhelp.product.entity.Product;
import com.example.sellerhelp.product.repository.ProductRepository;
import com.example.sellerhelp.security.SecurityService;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomerSaleService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final ProductRepository productRepository;
    private final DealerStockRepository dealerStockRepository;
    private final CustomerOrderRepository customerOrderRepository;
    private final CustomerInvoiceRepository customerInvoiceRepository;
    private final CustomerDealerMappingRepository customerDealerMappingRepository;
    private final SecurityService securityService;
    private final EmailService emailService;
    private final EntityManager entityManager;

    public CustomerInvoiceDto createCustomerSale(CreateCustomerSaleDto dto) {
        User dealer = securityService.getCurrentUser();
        CustomerDetailsDto customerDetails = dto.getCustomer();

        // Step 1: Find or create the Customer User record.
        User customer = userRepository.findByEmail(customerDetails.getEmail())
                .orElseGet(() -> createNewCustomer(customerDetails));

        // Step 2: Create the mapping between this Dealer and Customer.
        createCustomerDealerMapping(dealer, customer);

        // Step 3: Create the Customer Order and check/decrement the Dealer's stock.
        CustomerOrder order = CustomerOrder.builder()
                .customer(customer)
                .build();

        BigDecimal totalPrice = BigDecimal.ZERO;
        List<CustomerOrderMapping> mappings = new ArrayList<>();

        for (OrderItemDto item : dto.getProducts()) {
            Product product = productRepository.findByProductId(item.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + item.getProductId()));

            // --- CRITICAL LOGIC: Check and decrement the DEALER's stock ---
            DealerStock dealerStock = dealerStockRepository.findByDealerAndProduct(dealer, product)
                    .orElseThrow(() -> new ConflictException("You do not have stock for product: " + product.getName()));

            if (dealerStock.getQuantity() < item.getQuantity()) {
                throw new ConflictException("Insufficient stock for '" + product.getName() +
                        "'. Required: " + item.getQuantity() + ", You have: " + dealerStock.getQuantity());
            }
            dealerStock.setQuantity(dealerStock.getQuantity() - item.getQuantity());
            dealerStockRepository.save(dealerStock);

            // --- End of stock logic ---

            BigDecimal itemPrice = product.getUnitPrice().multiply(new BigDecimal(item.getQuantity()));
            totalPrice = totalPrice.add(itemPrice);

            CustomerOrderMapping mapping = CustomerOrderMapping.builder()
                    .order(order)
                    .product(product)
                    .quantity(item.getQuantity())
                    .unitPrice(product.getUnitPrice())
                    .build();
            mappings.add(mapping);
        }

        order.setTotalPrice(totalPrice);
        order.setItems(mappings);
        CustomerOrder savedOrder = customerOrderRepository.saveAndFlush(order);
        entityManager.refresh(savedOrder);

        // Step 4: Create the Customer Invoice
        CustomerInvoice invoice = CustomerInvoice.builder()
                .customer(customer)
                .order(savedOrder)
                .build();

        CustomerInvoice savedInvoice = customerInvoiceRepository.saveAndFlush(invoice);
        entityManager.refresh(savedInvoice);

        return toDto(savedInvoice, dealer);
    }

    private User createNewCustomer(CustomerDetailsDto customerDetails) {
        Role customerRole = roleRepository.findByName(UserRole.CUSTOMER)
                .orElseThrow(() -> new IllegalStateException("CUSTOMER role not found. Please seed roles."));

        // Generate a random temporary password
        String tempPassword = RandomStringUtils.randomAlphanumeric(10);

        User newCustomer = User.builder()
                .name(customerDetails.getName())
                .email(customerDetails.getEmail())
                .phone(customerDetails.getPhone())
                .password(passwordEncoder.encode(tempPassword))
                .role(customerRole)
                .build();

        User savedCustomer = userRepository.saveAndFlush(newCustomer);
        entityManager.refresh(savedCustomer);

        // Send welcome email with temporary password
        String subject = "Welcome to SellerHelp! Your account has been created.";
        String body = String.format(
                "<p>Hello %s,</p>" +
                        "<p>An account has been created for you by one of our dealers.</p>" +
                        "<p>You can log in using the following credentials:</p>" +
                        "<ul>" +
                        "<li><b>Username:</b> %s</li>" +
                        "<li><b>Temporary Password:</b> %s</li>" +
                        "</ul>" +
                        "<p>We recommend you change your password after your first login.</p>",
                savedCustomer.getName(),
                savedCustomer.getEmail(),
                tempPassword
        );
        emailService.sendEmail(savedCustomer.getEmail(), subject, body);

        return savedCustomer;
    }

    private void createCustomerDealerMapping(User dealer, User customer) {
        if (!customerDealerMappingRepository.existsByDealerAndCustomer(dealer, customer)) {
            CustomerDealerMapping mapping = CustomerDealerMapping.builder()
                    .dealer(dealer)
                    .customer(customer)
                    .build();
            customerDealerMappingRepository.save(mapping);
        }
    }

    private CustomerInvoiceDto toDto(CustomerInvoice invoice, User dealer) {
        return CustomerInvoiceDto.builder()
                .invoiceId(invoice.getInvoiceId())
                .orderId(invoice.getOrder().getOrderId())
                .customerId(invoice.getCustomer().getUserId())
                .customerName(invoice.getCustomer().getName())
                .dealerId(dealer.getUserId())
                .dealerName(dealer.getName())
                .createdAt(invoice.getCreatedAt())
                .build();
    }
}