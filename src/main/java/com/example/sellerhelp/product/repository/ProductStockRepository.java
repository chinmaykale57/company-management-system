package com.example.sellerhelp.product.repository;

import com.example.sellerhelp.factory.entity.Factory;
import com.example.sellerhelp.product.entity.Product;
import com.example.sellerhelp.product.entity.ProductStock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductStockRepository extends JpaRepository<ProductStock, Long> {
    Optional<ProductStock> findByFactoryAndProduct(Factory factory, Product product);
    Page<ProductStock> findByFactory(Factory factory, Pageable pageable);
    Page<ProductStock> findByProduct(Product product, Pageable pageable);
}