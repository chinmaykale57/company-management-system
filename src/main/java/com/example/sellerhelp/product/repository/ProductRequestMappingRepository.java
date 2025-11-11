package com.example.sellerhelp.product.repository;

import com.example.sellerhelp.product.entity.ProductRequestMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRequestMappingRepository extends JpaRepository<ProductRequestMapping, Long> {
    // We can add more specific query methods here later if needed
}