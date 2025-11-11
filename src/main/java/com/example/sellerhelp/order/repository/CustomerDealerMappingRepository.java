package com.example.sellerhelp.order.repository;

import com.example.sellerhelp.appuser.entity.User;
import com.example.sellerhelp.order.entity.CustomerDealerMapping;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerDealerMappingRepository extends JpaRepository<CustomerDealerMapping, Long> {

    // This will be useful later for viewing a distributor's customers
    Page<CustomerDealerMapping> findByDealer(User dealer, Pageable pageable);

    boolean existsByDealerAndCustomer(User dealer, User customer);
}