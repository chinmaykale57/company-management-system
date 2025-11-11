package com.example.sellerhelp.product.entity;

import com.example.sellerhelp.factory.entity.Factory;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "product_stock")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProductStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "factory_id", nullable = false)
    private Factory factory;

    @Builder.Default
    @Column(nullable = false)
    private Long quantity = 0L;
}