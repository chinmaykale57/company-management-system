package com.example.sellerhelp.tool.repository;

import com.example.sellerhelp.tool.entity.Bucket;
import com.example.sellerhelp.tool.entity.Tool;
import com.example.sellerhelp.tool.entity.ToolsBucketMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ToolsBucketMappingRepository extends JpaRepository<ToolsBucketMapping, Long> {
    Optional<ToolsBucketMapping> findByToolAndBucket(Tool tool, Bucket bucket);
}