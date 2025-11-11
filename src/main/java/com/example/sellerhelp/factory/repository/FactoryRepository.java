package com.example.sellerhelp.factory.repository;

import com.example.sellerhelp.factory.entity.Factory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FactoryRepository extends JpaRepository<Factory, Long>, JpaSpecificationExecutor<Factory> {

        Optional<Factory> findByFactoryId(String factoryId);

        boolean existsByNameIgnoreCase(String name);
}

