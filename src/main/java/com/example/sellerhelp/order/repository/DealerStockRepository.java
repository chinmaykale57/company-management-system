package com.example.sellerhelp.order.repository;

import com.example.sellerhelp.appuser.entity.User;
import com.example.sellerhelp.order.entity.DealerStock;
import com.example.sellerhelp.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DealerStockRepository extends JpaRepository<DealerStock, Long> {

    Optional<DealerStock> findByDealerAndProduct(User dealer, Product product);
}