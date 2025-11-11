package com.example.sellerhelp.tool.repository;

import com.example.sellerhelp.tool.entity.Tool;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.Optional;

public interface ToolRepository extends JpaRepository<Tool, Long> {
    boolean existsByNameIgnoreCase(@NotBlank(message = "Tool name is required.") @Size(min = 3, max = 150) String name);

    Optional<Tool> findByToolId(@NotBlank(message = "Tool ID is required.") String toolId);
}

