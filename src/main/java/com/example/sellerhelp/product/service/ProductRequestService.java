package com.example.sellerhelp.product.service;

import com.example.sellerhelp.appuser.dto.PageableDto;
import com.example.sellerhelp.appuser.entity.User;
import com.example.sellerhelp.constant.ProductRequestStatus;
import com.example.sellerhelp.exception.BadRequestException;
import com.example.sellerhelp.exception.ResourceNotFoundException;
import com.example.sellerhelp.factory.entity.Factory;
import com.example.sellerhelp.factory.repository.FactoryRepository;
import com.example.sellerhelp.notification.service.EmailService;
import com.example.sellerhelp.product.dto.CreateProductRequestDto;
import com.example.sellerhelp.product.dto.ProductRequestDto;
import com.example.sellerhelp.product.dto.ProductRequestItemDto;
import com.example.sellerhelp.product.entity.Product;
import com.example.sellerhelp.product.entity.ProductRequest;
import com.example.sellerhelp.product.entity.ProductRequestMapping;
import com.example.sellerhelp.product.repository.ProductRepository;
import com.example.sellerhelp.product.repository.ProductRequestMappingRepository;
import com.example.sellerhelp.product.repository.ProductRequestRepository;
import com.example.sellerhelp.security.SecurityService;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductRequestService {

    private final ProductRequestRepository productRequestRepository;
    private final ProductRequestMappingRepository productRequestMappingRepository;
    private final ProductRepository productRepository;
    private final FactoryRepository factoryRepository;
    private final SecurityService securityService;
    private final EntityManager entityManager;
    private final EmailService emailService;

    @Transactional
    public ProductRequestDto createProductRequest(CreateProductRequestDto dto) {
        User centralOfficer = securityService.getCurrentUser();
        Factory factory = factoryRepository.findByFactoryId(dto.getFactoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Factory not found with ID: " + dto.getFactoryId()));

        ProductRequest request = ProductRequest.builder()
                .centralOfficer(centralOfficer)
                .factory(factory)
                .status(ProductRequestStatus.REQUESTED)
                .build();

        ProductRequest savedRequest = productRequestRepository.saveAndFlush(request);
        entityManager.refresh(savedRequest);

        List<ProductRequestMapping> mappings = new ArrayList<>();
        for (ProductRequestItemDto item : dto.getProducts()) {
            Product product = productRepository.findByProductId(item.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + item.getProductId()));

            ProductRequestMapping mapping = ProductRequestMapping.builder()
                    .request(savedRequest)
                    .product(product)
                    .quantity(item.getQuantity())
                    .build();
            mappings.add(mapping);
        }

        productRequestMappingRepository.saveAll(mappings);
        savedRequest.setProductMappings(mappings);

        User plantHead = factory.getPlantHead();
        if (plantHead != null && plantHead.getEmail() != null) {
            String subject = "New Product Request Received: " + savedRequest.getRequestNumber();
            String body = String.format(
                    "<p>Hello %s,</p>" +
                            "<p>A new product request from the Central Office is assigned to your factory, <b>%s</b>.</p>" +
                            "<p>Request Number: <b>%s</b></p>" +
                            "<p>Please log in to the SellerHelp application to review and approve the request.</p>",
                    plantHead.getName(),
                    factory.getName(),
                    savedRequest.getRequestNumber()
            );
            emailService.sendEmail(plantHead.getEmail(), subject, body);
        }

        return toDto(savedRequest);
    }

    @Transactional
    public ProductRequestDto approveRequest(Long requestId) {
        ProductRequest request = productRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Product Request not found with ID: " + requestId));

        if (request.getStatus() != ProductRequestStatus.REQUESTED) {
            throw new BadRequestException("Only a REQUESTED product request can be approved.");
        }

        request.setStatus(ProductRequestStatus.FULFILLED);
        ProductRequest savedRequest = productRequestRepository.save(request);


        User centralOfficer = savedRequest.getCentralOfficer();
        String subject = "Product Request Approved: " + savedRequest.getRequestNumber();
        String body = String.format(
                "<p>Hello %s,</p>" +
                        "<p>Your product request <b>%s</b> for factory <b>%s</b> has been approved.</p>",
                centralOfficer.getName(),
                savedRequest.getRequestNumber(),
                savedRequest.getFactory().getName()
        );
        emailService.sendEmail(centralOfficer.getEmail(), subject, body);

        return toDto(savedRequest);
    }

    @Transactional
    public ProductRequestDto rejectRequest(Long requestId) {
        ProductRequest request = productRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Product Request not found with ID: " + requestId));

        if (request.getStatus() != ProductRequestStatus.REQUESTED) {
            throw new BadRequestException("Only a REQUESTED product request can be rejected.");
        }

        request.setStatus(ProductRequestStatus.REJECTED);
        ProductRequest savedRequest = productRequestRepository.save(request);

        User centralOfficer = savedRequest.getCentralOfficer();
        String subject = "Product Request Rejected: " + savedRequest.getRequestNumber();
        String body = String.format(
                "<p>Hello %s,</p>" +
                        "<p>Your product request <b>%s</b> for factory <b>%s</b> has been rejected.</p>" +
                        "<p>Please log in for more details.</p>",
                centralOfficer.getName(),
                savedRequest.getRequestNumber(),
                savedRequest.getFactory().getName()
        );
        emailService.sendEmail(centralOfficer.getEmail(), subject, body);

        return toDto(savedRequest);
    }

    public Page<ProductRequestDto> getAllProductRequests(PageableDto pageReq) {
        Pageable pageable = PageRequest.of(pageReq.getPage(), pageReq.getSize(), Sort.by("createdAt").descending());
        return productRequestRepository.findAll(pageable).map(this::toDto);
    }

    public Page<ProductRequestDto> getProductRequestsByFactory(String factoryId, PageableDto pageReq) {
        Factory factory = factoryRepository.findByFactoryId(factoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Factory not found with ID: " + factoryId));
        Pageable pageable = PageRequest.of(pageReq.getPage(), pageReq.getSize(), Sort.by("createdAt").descending());
        return productRequestRepository.findByFactory(factory, pageable).map(this::toDto);
    }

    public ProductRequestDto getProductRequestById(Long requestId) {
        return productRequestRepository.findById(requestId)
                .map(this::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Product Request not found with ID: " + requestId));
    }

    private ProductRequestDto toDto(ProductRequest request) {
        List<ProductRequestItemDto> items = request.getProductMappings().stream()
                .map(mapping -> {
                    ProductRequestItemDto itemDto = new ProductRequestItemDto();
                    itemDto.setProductId(mapping.getProduct().getProductId());
                    itemDto.setQuantity(mapping.getQuantity());
                    return itemDto;
                }).toList();

        return ProductRequestDto.builder()
                .id(request.getId())
                .requestNumber(request.getRequestNumber())
                .centralOfficerName(request.getCentralOfficer().getName())
                .factoryId(request.getFactory().getFactoryId())
                .factoryName(request.getFactory().getName())
                .status(request.getStatus())
                .createdAt(request.getCreatedAt())
                .products(items)
                .build();
    }
}