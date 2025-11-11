package com.example.sellerhelp.controller;

import com.example.sellerhelp.appuser.dto.PageableDto;
import com.example.sellerhelp.common.ApiResponseDto;
import com.example.sellerhelp.product.dto.CreateProductDto;
import com.example.sellerhelp.product.dto.ProductCategoryDto;
import com.example.sellerhelp.product.dto.ProductDto;
import com.example.sellerhelp.product.dto.UpdateProductDto;
import com.example.sellerhelp.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // --- CATEGORY ENDPOINTS (ADMIN) ---

    @PostMapping("/categories")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto<ProductCategoryDto>> createCategory(@Valid @RequestBody ProductCategoryDto dto) {
        ProductCategoryDto createdCategory = productService.createProductCategory(dto);
        return new ResponseEntity<>(ApiResponseDto.ok(createdCategory, "Product category created successfully."), HttpStatus.CREATED);
    }

    @GetMapping("/categories")
    @PreAuthorize("isAuthenticated()") // Any authenticated user can view categories
    public ResponseEntity<ApiResponseDto<List<ProductCategoryDto>>> getAllCategories() {
        List<ProductCategoryDto> categories = productService.getAllProductCategories();
        return ResponseEntity.ok(ApiResponseDto.ok(categories));
    }

    // --- PRODUCT ENDPOINTS (ADMIN) ---

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto<ProductDto>> createProduct(@Valid @RequestBody CreateProductDto dto) {
        ProductDto createdProduct = productService.createProduct(dto);
        return new ResponseEntity<>(ApiResponseDto.ok(createdProduct, "Product created successfully."), HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()") // Any authenticated user can browse products
    public ResponseEntity<ApiResponseDto<Page<ProductDto>>> getAllProducts(@ModelAttribute PageableDto pageableDto) {
        Page<ProductDto> products = productService.getAllProducts(pageableDto);
        return ResponseEntity.ok(ApiResponseDto.ok(products));
    }

    @GetMapping("/{productId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponseDto<ProductDto>> getProductById(@PathVariable String productId) {
        ProductDto product = productService.getProductById(productId);
        return ResponseEntity.ok(ApiResponseDto.ok(product));
    }

    @PutMapping("/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto<ProductDto>> updateProduct(
            @PathVariable String productId,
            @Valid @RequestBody UpdateProductDto dto) {
        ProductDto updatedProduct = productService.updateProduct(productId, dto);
        return ResponseEntity.ok(ApiResponseDto.ok(updatedProduct, "Product updated successfully."));
    }

    @DeleteMapping("/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto<Void>> deactivateProduct(@PathVariable String productId) {
        productService.deactivateProduct(productId);
        return ResponseEntity.ok(ApiResponseDto.ok(null, "Product deactivated successfully."));
    }
}