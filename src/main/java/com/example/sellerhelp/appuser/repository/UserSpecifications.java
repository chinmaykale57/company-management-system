package com.example.sellerhelp.appuser.repository;

import com.example.sellerhelp.appuser.dto.UserFilterDto;
import com.example.sellerhelp.appuser.entity.User;
import com.example.sellerhelp.constant.ActiveStatus;
import com.example.sellerhelp.constant.UserRole;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public final class UserSpecifications {

    private UserSpecifications() {}

    //Public Entry Points

    public static Specification<User> withFilter(UserFilterDto filter) {
        List<Specification<User>> specs = new ArrayList<>();

        if (StringUtils.hasText(filter.getName())) {
            specs.add(hasName(filter.getName()));
        }
        if (StringUtils.hasText(filter.getEmail())) {
            specs.add(hasEmail(filter.getEmail()));
        }
        if (StringUtils.hasText(filter.getPhone())) {
            specs.add(hasPhone(filter.getPhone()));
        }
        if (filter.getStatus() != null) {
            specs.add(hasStatus(filter.getStatus()));
        }
        if (StringUtils.hasText(filter.getRoleId())) {
            specs.add(hasRoleId(filter.getRoleId()));
        }
        if (StringUtils.hasText(filter.getFactoryId())) {
            specs.add(isInFactory(filter.getFactoryId()));
        }
        if (StringUtils.hasText(filter.getBayId())) {
            specs.add(isInBay(filter.getBayId()));
        }

        return combineAll(specs);
    }

    public static Specification<User> globalSearch(String query) {
        if (!StringUtils.hasText(query)) {
            return (root, cq, cb) -> cb.conjunction(); // Always true
        }

        String lowerQuery = query.toLowerCase();
        String pattern = "%" + lowerQuery + "%";
        String phonePattern = "%" + query + "%";

        return (root, cq, cb) -> cb.or(
                cb.like(cb.lower(root.get("name")), pattern),
                cb.like(cb.lower(root.get("email")), pattern),
                cb.like(root.get("phone"), phonePattern),
                cb.like(cb.lower(root.get("userId")), pattern)
        );
    }

    public static Specification<User> isWorkerInFactory(String factoryId) {
        return (root, query, cb) -> {
            var mapping = root.join("factoryMappings", JoinType.INNER);
            var factoryMatch = cb.equal(mapping.get("factory").get("factoryId"), factoryId);
            var roleMatch = root.get("role").get("name").in(UserRole.WORKER, UserRole.CHIEF_SUPERVISOR);
            return cb.and(factoryMatch, roleMatch);
        };
    }

    public static Specification<User> isWorkerInBay(String bayId) {
        return (root, query, cb) -> {
            var mapping = root.join("factoryMappings", JoinType.INNER);
            var bayMatch = cb.equal(mapping.get("bay").get("bayId"), bayId);
            var roleMatch = cb.equal(root.get("role").get("name"), UserRole.WORKER);
            return cb.and(bayMatch, roleMatch);
        };
    }

    // Helper Specifications

    private static Specification<User> hasName(String name) {
        return likeIgnoreCase("name", name);
    }

    private static Specification<User> hasEmail(String email) {
        return likeIgnoreCase("email", email);
    }

    private static Specification<User> hasPhone(String phone) {
        return (root, query, cb) -> cb.like(root.get("phone"), "%" + phone + "%");
    }

    private static Specification<User> hasStatus(ActiveStatus status) {
        return (root, query, cb) -> cb.equal(root.get("isActive"), status);
    }

    private static Specification<User> hasRoleId(String roleId) {
        return (root, query, cb) -> cb.equal(root.get("role").get("id"), Long.parseLong(roleId));
    }

    private static Specification<User> isInFactory(String factoryId) {
        return hasJoinMapping("factory", "factoryId", factoryId);
    }

    private static Specification<User> isInBay(String bayId) {
        return hasJoinMapping("bay", "bayId", bayId);
    }

    // === Utility Methods ===

    private static Specification<User> likeIgnoreCase(String field, String value) {
        String pattern = "%" + value.toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get(field)), pattern);
    }

    private static Specification<User> hasJoinMapping(String joinField, String idField, String idValue) {
        return (root, query, cb) -> {
            var subquery = query.subquery(Long.class);
            var subRoot = subquery.from(User.class);
            var mapping = subRoot.join("factoryMappings");
            subquery.select(subRoot.get("id"))
                    .where(
                            cb.equal(subRoot.get("id"), root.get("id")),
                            cb.equal(mapping.get(joinField).get(idField), idValue)
                    );
            return cb.exists(subquery);
        };
    }

    private static Specification<User> combineAll(List<Specification<User>> specs) {
        return specs.stream()
                .reduce(Specification::and)
                .orElse((root, query, cb) -> cb.conjunction());
    }
}