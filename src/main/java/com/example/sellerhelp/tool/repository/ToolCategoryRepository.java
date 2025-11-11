package com.example.sellerhelp.tool.repository;

import com.example.sellerhelp.tool.entity.ToolCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ToolCategoryRepository extends JpaRepository<ToolCategory, Long> {

    boolean existsByNameIgnoreCase(@NotBlank(message = "Category name is required.") @Size(min = 3, max = 150) String name);
}
