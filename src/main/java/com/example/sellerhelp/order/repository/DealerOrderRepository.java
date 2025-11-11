package com.example.sellerhelp.order.repository;

import com.example.sellerhelp.constant.DealerOrderStatus;
import com.example.sellerhelp.order.entity.DealerOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface DealerOrderRepository extends JpaRepository<DealerOrder, Long>, JpaSpecificationExecutor<DealerOrder> {
    Optional<DealerOrder> findByOrderId(String orderId);
    long countByStatus(DealerOrderStatus status);

    @Query("SELECT COALESCE(SUM(o.totalPrice), 0) FROM DealerOrder o WHERE o.status = :status AND o.createdAt >= :startDate")
    BigDecimal sumTotalPriceByStatusAndDate(
            @Param("status") DealerOrderStatus status,
            @Param("startDate") LocalDateTime startDate
    );
}