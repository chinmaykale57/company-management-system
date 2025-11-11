package com.example.sellerhelp.factory.repository;

import com.example.sellerhelp.appuser.entity.User;
import com.example.sellerhelp.factory.entity.Factory;
import com.example.sellerhelp.factory.entity.UserFactoryMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserFactoryMappingRepository extends JpaRepository<UserFactoryMapping, Long> {
    boolean existsByUserAndFactory(User user, Factory factory);
}