package com.example.sellerhelp.product.repository;

import com.example.sellerhelp.factory.entity.Factory;
import com.example.sellerhelp.product.entity.FactoryProduction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FactoryProductionRepository extends JpaRepository<FactoryProduction, Long> {
    Page<FactoryProduction> findByFactory(Factory factory, Pageable pageable);
}