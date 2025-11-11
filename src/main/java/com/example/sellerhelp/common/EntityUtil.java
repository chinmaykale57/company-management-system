package com.example.sellerhelp.common;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** currently this file is useless but im planning to integrate in future **/

@Component
public class EntityUtil {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public <T> T saveAndRefresh(T entity) {
        entityManager.persist(entity);
        entityManager.flush();     // force SQL insert to execute
        entityManager.refresh(entity); // re-sync DB trigger-generated values
        return entity;
    }
}
