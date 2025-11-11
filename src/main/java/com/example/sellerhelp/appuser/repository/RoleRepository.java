package com.example.sellerhelp.appuser.repository;

import com.example.sellerhelp.appuser.entity.Role;
import com.example.sellerhelp.constant.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * Finds a role by its unique name.
     * This is case-sensitive, so ensure the name matches the database entry exactly.
     *
     * @param name The name of the role (e.g., "OWNER", "DISTRIBUTOR").
     * @return An Optional containing the Role if found, otherwise empty.
     */
    Optional<Role> findByName(UserRole name);
}