package com.example.sellerhelp.product.service;

import com.example.sellerhelp.appuser.dto.PageableDto;
import com.example.sellerhelp.appuser.entity.User;
import com.example.sellerhelp.exception.ResourceNotFoundException;
import com.example.sellerhelp.factory.entity.Factory;
import com.example.sellerhelp.factory.entity.UserFactoryMapping;
import com.example.sellerhelp.factory.repository.FactoryRepository;
import com.example.sellerhelp.product.dto.FactoryProductionDto;
import com.example.sellerhelp.product.dto.ProductStockDto;
import com.example.sellerhelp.product.dto.RecordProductionDto;
import com.example.sellerhelp.product.entity.FactoryProduction;
import com.example.sellerhelp.product.entity.Product;
import com.example.sellerhelp.product.entity.ProductStock;
import com.example.sellerhelp.product.repository.FactoryProductionRepository;
import com.example.sellerhelp.product.repository.ProductRepository;
import com.example.sellerhelp.product.repository.ProductStockRepository;
import com.example.sellerhelp.security.SecurityService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductionService {

    private final ProductRepository productRepository;
    private final ProductStockRepository productStockRepository;
    private final FactoryProductionRepository factoryProductionRepository;
    private final FactoryRepository factoryRepository;
    private final SecurityService securityService;

    // --- PRODUCTION & STOCK MANAGEMENT (PLANT_HEAD) ---

    @Transactional
    public FactoryProductionDto recordProduction(RecordProductionDto dto) {
        // Now this is much cleaner!
        User currentUser = securityService.getCurrentUser();

        Factory factory = currentUser.getFactoryMappings().stream()
                .map(UserFactoryMapping::getFactory)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("You are not assigned to a factory and cannot record production."));
        Product product = productRepository.findByProductId(dto.getProductId())
                .orElseThrow(() -> new NoSuchElementException("Product not found with ID: " + dto.getProductId()));

        FactoryProduction productionRecord = FactoryProduction.builder()
                .factory(factory)
                .product(product)
                .productionQuantity(dto.getProductionQuantity())
                .productionDate(dto.getProductionDate())
                .build();

        FactoryProduction savedRecord = factoryProductionRepository.save(productionRecord);

        ProductStock stock = productStockRepository.findByFactoryAndProduct(factory, product)
                .orElseGet(() -> ProductStock.builder()
                        .factory(factory)
                        .product(product)
                        .build());

        stock.setQuantity(stock.getQuantity() + dto.getProductionQuantity());
        productStockRepository.save(stock);

        return toDto(savedRecord);
    }

    // --- VIEWING METHODS ---

    public Page<ProductStockDto> getStockByFactory(String factoryId, PageableDto pageReq) {
        Factory factory = factoryRepository.findByFactoryId(factoryId)
                .orElseThrow(() -> new NoSuchElementException("Factory not found with ID: " + factoryId));

        Pageable pageable = PageRequest.of(pageReq.getPage(), pageReq.getSize(), Sort.by("product.name"));
        return productStockRepository.findByFactory(factory, pageable).map(this::toDto);
    }

    public Page<FactoryProductionDto> getProductionRecordsByFactory(String factoryId, PageableDto pageReq) {
        Factory factory = factoryRepository.findByFactoryId(factoryId)
                .orElseThrow(() -> new NoSuchElementException("Factory not found with ID: " + factoryId));

        Pageable pageable = PageRequest.of(pageReq.getPage(), pageReq.getSize(), Sort.by("productionDate").descending());
        return factoryProductionRepository.findByFactory(factory, pageable).map(this::toDto);
    }

    /**
     * Retrieves a list of stock levels for a specific product across all factories.
     * This provides the visibility needed for the Central Office to make fulfillment decisions.
     * @param productId The public ID of the product to check.
     * @return A List of ProductStockDto objects, one for each factory that stocks the product.
     */
    public Page<ProductStockDto> getStockLevelsForProduct(String productId, PageableDto pageReq) {
        Product product = productRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));

        Pageable pageable = PageRequest.of(pageReq.getPage(), pageReq.getSize(), Sort.by("quantity").descending());
        Page<ProductStock> stockPage = productStockRepository.findByProduct(product, pageable);

        return stockPage.map(this::toDto);
    }


    //Helpers
    private ProductStockDto toDto(ProductStock stock) {
        return ProductStockDto.builder()
                .productId(stock.getProduct().getProductId())
                .productName(stock.getProduct().getName())
                .factoryId(stock.getFactory().getFactoryId())
                .factoryName(stock.getFactory().getName())
                .quantity(stock.getQuantity())
                .build();
    }

    private FactoryProductionDto toDto(FactoryProduction production) {
        return FactoryProductionDto.builder()
                .id(production.getId())
                .productId(production.getProduct().getProductId())
                .productName(production.getProduct().getName())
                .factoryId(production.getFactory().getFactoryId())
                .factoryName(production.getFactory().getName())
                .productionQuantity(production.getProductionQuantity())
                .productionDate(production.getProductionDate())
                .build();
    }
}