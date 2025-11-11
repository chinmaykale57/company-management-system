package com.example.sellerhelp.product.entity;

import com.example.sellerhelp.factory.entity.Factory;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "factory_production")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FactoryProduction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "factory_id", nullable = false)
    private Factory factory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "production_quantity", nullable = false)
    private Long productionQuantity;

    @Column(name = "production_date", nullable = false)
    private LocalDate productionDate;
}