package com.example.sellerhelp.factory.repository;

import com.example.sellerhelp.factory.dto.FactoryFilterDto;
import com.example.sellerhelp.factory.entity.Factory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class FactorySpecifications {

    public static Specification<Factory> withFilter(FactoryFilterDto filter) {
        Specification<Factory> spec = Specification.where(null);

        if (StringUtils.hasText(filter.getName())) {
            spec = spec.and(hasName(filter.getName()));
        }
        if (StringUtils.hasText(filter.getCity())) {
            spec = spec.and(hasCity(filter.getCity()));
        }
        if (StringUtils.hasText(filter.getPlantHeadName())) {
            spec = spec.and(hasPlantHeadName(filter.getPlantHeadName()));
        }
        return spec;
    }

    private static Specification<Factory> hasName(String name) {
        return (root, query, cb) -> cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
    }

    private static Specification<Factory> hasCity(String city) {
        return (root, query, cb) -> cb.like(cb.lower(root.get("city")), "%" + city.toLowerCase() + "%");
    }

    private static Specification<Factory> hasPlantHeadName(String plantHeadName) {
        // This requires a join to the users table
        return (root, query, cb) -> {
            var plantHeadJoin = root.join("plantHead");
            return cb.like(cb.lower(plantHeadJoin.get("name")), "%" + plantHeadName.toLowerCase() + "%");
        };
    }
}