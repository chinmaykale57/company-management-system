package com.example.sellerhelp.appuser.repository;

import com.example.sellerhelp.appuser.entity.Role;
import com.example.sellerhelp.appuser.entity.User;
import com.example.sellerhelp.constant.ActiveStatus;
import com.example.sellerhelp.constant.UserRole;
import com.example.sellerhelp.factory.entity.Factory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    // SIMPLE DERIVED QUERIES (KEEP THESE)
    Optional<User> findByUserId(String userId);
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByUserId(String userId);
    // NEW DERIVED QUERY to replace the @Query version for dealers
    Page<User> findByRole_NameAndIsActive(UserRole roleName, ActiveStatus status, Pageable pageable);

    Page<User> findByRole_Name(UserRole roleName, Pageable pageable);

    // SIMPLE DERIVED QUERIES FOR DASHBOARD COUNTS
    long countByIsActive(ActiveStatus status);
    long countByRole_NameAndIsActive(UserRole roleName, ActiveStatus status);

    @Query("SELECT u FROM User u JOIN u.factoryMappings fm WHERE fm.factory = :factory AND u.role.name = :roleName")
    List<User> findUsersByFactoryAndRole(@Param("factory") Factory factory, @Param("roleName") UserRole roleName);


    //below queries are temporary for now, will remove once flow is complete and jpa can take over
    @Modifying
    @Query("UPDATE User u SET u.isActive = :status WHERE u.userId = :userId")
    int updateUserStatus(@Param("userId") String userId, @Param("status") ActiveStatus status);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE User u SET u.name = :name, u.phone = :phone, u.imageUrl = :imageUrl, u.role = :role WHERE u.userId = :userId")
    void updateUserProfile(
            @Param("userId") String userId,
            @Param("name") String name,
            @Param("phone") String phone,
            @Param("imageUrl") String imageUrl,
            @Param("role") Role role
    );

    @Modifying
    @Query("UPDATE User u SET u.imageUrl )
            void

    // DELETED
    // - findAllUsers(...)
    // - findWorkersInMyFactory(...)
    // - findWorkersInMyBay(...)
    // - findAllDealers(...) -> replaced by the derived query above
    // - searchGlobally(...)
}