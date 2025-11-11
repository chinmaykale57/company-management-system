package com.example.sellerhelp.tool.repository;

import com.example.sellerhelp.appuser.entity.User;
import com.example.sellerhelp.constant.ToolIssuanceStatus;
import com.example.sellerhelp.factory.entity.Factory;
import com.example.sellerhelp.tool.entity.ToolIssuance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ToolIssuanceRepository extends JpaRepository<ToolIssuance, Long> {
    // In ToolIssuanceRepository.java
    Page<ToolIssuance> findByWorkerAndStatusIn(User worker, List<ToolIssuanceStatus> statuses, Pageable pageable);

    @Query("SELECT ti FROM ToolIssuance ti WHERE ti.factory = :factory " +
            "AND ti.returnDate < :overdueDate " +
            "AND ti.status IN :statuses")
    Page<ToolIssuance> findOverdueTools(
            @Param("factory") Factory factory,
            @Param("overdueDate") LocalDateTime overdueDate,
            @Param("statuses") List<ToolIssuanceStatus> statuses,
            Pageable pageable
    );

}
