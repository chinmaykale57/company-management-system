package com.example.sellerhelp.order.repository;

import com.example.sellerhelp.dashboard.dto.ProductIdSalesDto;
import com.example.sellerhelp.order.entity.DealerOrderMapping;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DealerOrderMappingRepository extends JpaRepository<DealerOrderMapping, Long> {
    @Query("SELECT new com.example.sellerhelp.dashboard.dto.ProductIdSalesDto(m.product.id, SUM(m.quantity)) " +
            "FROM DealerOrderMapping m " +
            "WHERE m.order.status = com.example.sellerhelp.constant.DealerOrderStatus.APPROVED " + // Only count sales from approved orders
            "GROUP BY m.product.id " +
            "ORDER BY SUM(m.quantity) DESC") // DESC for top sellers
    List<ProductIdSalesDto> findFastMovingProductIds(Pageable pageable);

    @Query("SELECT new com.example.sellerhelp.dashboard.dto.ProductIdSalesDto(m.product.id, SUM(m.quantity)) " +
            "FROM DealerOrderMapping m " +
            "WHERE m.order.status = com.example.sellerhelp.constant.DealerOrderStatus.APPROVED " + // Only count sales from approved orders
            "GROUP BY m.product.id " +
            "ORDER BY SUM(m.quantity) ASC") // ASC for worst sellers
    List<ProductIdSalesDto> findSlowMovingProductIds(Pageable pageable);
}