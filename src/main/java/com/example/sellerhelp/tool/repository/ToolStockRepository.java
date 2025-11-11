package com.example.sellerhelp.tool.repository;

import com.example.sellerhelp.factory.entity.Factory;
import com.example.sellerhelp.tool.entity.Tool;
import com.example.sellerhelp.tool.entity.ToolStock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ToolStockRepository extends JpaRepository<ToolStock, Long> {
    Optional<ToolStock> findByFactoryAndTool(Factory factory, Tool tool);
    Page<ToolStock> findByFactory(Factory factory, Pageable pageable);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE ToolStock ts SET ts.totalQuantity = ts.totalQuantity + :quantity, ts.availableQuantity = ts.availableQuantity + :quantity WHERE ts.id = :stockId")
    void incrementStock(@Param("stockId") Long stockId, @Param("quantity") Long quantity);

    @Modifying(clearAutomatically = true)
    @Query(value = "INSERT INTO tool_stock (factory_id, tool_id, total_quantity, available_quantity, issued_quantity, last_updated_at) VALUES (:factoryId, :toolId, :quantity, :quantity, 0, NOW())", nativeQuery = true)
    void insertNewStock(@Param("factoryId") Long factoryId, @Param("toolId") Long toolId, @Param("quantity") Long quantity);
}
