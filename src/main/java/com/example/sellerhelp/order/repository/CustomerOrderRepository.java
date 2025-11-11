package com.example.sellerhelp.order.repository;

import com.example.sellerhelp.appuser.entity.User;
import com.example.sellerhelp.order.entity.CustomerOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, Long> {
    // For the "My Purchases" view for a customer
    Page<CustomerOrder> findByCustomer(User customer, Pageable pageable);
}