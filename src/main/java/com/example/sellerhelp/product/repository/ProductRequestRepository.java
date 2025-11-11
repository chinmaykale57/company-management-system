package com.example.sellerhelp.product.repository;

import com.example.sellerhelp.constant.ProductRequestStatus;
import com.example.sellerhelp.factory.entity.Factory;
import com.example.sellerhelp.product.entity.ProductRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRequestRepository extends JpaRepository<ProductRequest, Long>, JpaSpecificationExecutor<ProductRequest> {

    // For the Plant Head to view requests for their factory
    Page<ProductRequest> findByFactory(Factory factory, Pageable pageable);

    long countByStatus(ProductRequestStatus productRequestStatus);
}