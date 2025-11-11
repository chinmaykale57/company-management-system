package com.example.sellerhelp.factory.repository;

import com.example.sellerhelp.factory.entity.FactoryBay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FactoryBayRepository extends JpaRepository<FactoryBay, Long> {
    Optional<FactoryBay> findByBayId(String bayId);
}