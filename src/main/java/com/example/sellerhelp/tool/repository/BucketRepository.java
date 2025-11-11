package com.example.sellerhelp.tool.repository;

import com.example.sellerhelp.tool.entity.Bucket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BucketRepository extends JpaRepository<Bucket, Long> {
    Optional<Bucket> findByBucketId(String bucketId);
    boolean existsByBucketId(String bucketId);
}