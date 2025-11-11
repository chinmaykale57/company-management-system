package com.example.sellerhelp.order.repository;

import com.example.sellerhelp.order.entity.CustomerOrderMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerOrderMappingRepository extends JpaRepository<CustomerOrderMapping, Long> {

}