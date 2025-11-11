package com.example.sellerhelp.product.service;

import com.example.sellerhelp.appuser.dto.PageableDto;
import com.example.sellerhelp.appuser.entity.User;
import com.example.sellerhelp.constant.ProductRequestStatus;
import com.example.sellerhelp.notification.service.EmailService;
import com.example.sellerhelp.product.dto.CreateProductDto;
import com.example.sellerhelp.product.dto.ProductCategoryDto;
import com.example.sellerhelp.product.dto.ProductDto;
import com.example.sellerhelp.product.dto.UpdateProductDto;
import com.example.sellerhelp.product.entity.Product;
import com.example.sellerhelp.product.entity.ProductCategory;
import com.example.sellerhelp.product.repository.ProductCategoryRepository;
import com.example.sellerhelp.product.repository.ProductRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final EntityManager entityManager;
    private final EmailService emailService;

    // --- CATEGORY MANAGEMENT (ADMIN) ---

    @Transactional
    public ProductCategoryDto createProductCategory(ProductCategoryDto dto) {
        if (productCategoryRepository.existsByNameIgnoreCase(dto.getName())) {
            throw new IllegalStateException("A product category with this name already exists.");
        }
        ProductCategory category = ProductCategory.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .build();
        return toDto(productCategoryRepository.save(category));
    }

    public List<ProductCategoryDto> getAllProductCategories() {
        return productCategoryRepository.findAll(Sort.by("name")).stream()
                .map(this::toDto)
                .toList();
    }

    // --- PRODUCT MANAGEMENT (ADMIN) ---

    @Transactional
    public ProductDto createProduct(CreateProductDto dto) {
        if (productRepository.existsByNameIgnoreCase(dto.getName())) {
            throw new IllegalStateException("A product with this name already exists.");
        }
        ProductCategory category = productCategoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Product category not found with ID: " + dto.getCategoryId()));

        Product product = Product.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .imageUrl(dto.getImageUrl())
                .unitPrice(dto.getUnitPrice())
                .category(category)
                .isActive(ProductRequestStatus.REQUESTED)
                .build();

        Product savedProduct = productRepository.saveAndFlush(product);
        entityManager.refresh(savedProduct);

        return toDto(savedProduct);
    }

    @Transactional
    public ProductDto updateProduct(String productId, UpdateProductDto dto) {
        Product product = productRepository.findByProductId(productId)
                .orElseThrow(() -> new NoSuchElementException("Product not found with ID: " + productId));

        if (StringUtils.hasText(dto.getName())) product.setName(dto.getName());
        if (StringUtils.hasText(dto.getDescription())) product.setDescription(dto.getDescription());
        if (StringUtils.hasText(dto.getImageUrl())) product.setImageUrl(dto.getImageUrl());
        if (dto.getUnitPrice() != null) product.setUnitPrice(dto.getUnitPrice());
        if (dto.getStatus() != null) product.setIsActive(dto.getStatus());

        if (dto.getCategoryId() != null) {
            ProductCategory newCategory = productCategoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("Product category not found with ID: " + dto.getCategoryId()));
            product.setCategory(newCategory);
        }

        return toDto(productRepository.save(product));
    }

    @Transactional
    public void deactivateProduct(String productId) {
        Product product = productRepository.findByProductId(productId)
                .orElseThrow(() -> new NoSuchElementException("Product not found with ID: " + productId));

        if (product.getIsActive() == ProductRequestStatus.INACTIVE) {
            throw new IllegalStateException("This product is already inactive.");
        }

        product.setIsActive(ProductRequestStatus.INACTIVE);
        productRepository.save(product);
    }

    public Page<ProductDto> getAllProducts(PageableDto pageReq) {
        Pageable pageable = PageRequest.of(pageReq.getPage(), pageReq.getSize(), Sort.by("name").ascending());
        return productRepository.findAll(pageable).map(this::toDto);
    }

    public ProductDto getProductById(String productId) {
        return productRepository.findByProductId(productId)
                .map(this::toDto)
                .orElseThrow(() -> new NoSuchElementException("Product not found with ID: " + productId));
    }

    // --- DTO CONVERTER METHODS ---

    private ProductCategoryDto toDto(ProductCategory category) {
        return ProductCategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .build();
    }

    private ProductDto toDto(Product product) {
        User createdBy = product.getCreatedBy();
        User updatedBy = product.getUpdatedBy();
        return ProductDto.builder()
                .productId(product.getProductId())
                .name(product.getName())
                .description(product.getDescription())
                .imageUrl(product.getImageUrl())
                .unitPrice(product.getUnitPrice())
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .status(product.getIsActive())
                .createdBy(createdBy != null ? createdBy.getName() : null)
                .updatedBy(updatedBy != null ? updatedBy.getName() : null)
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}