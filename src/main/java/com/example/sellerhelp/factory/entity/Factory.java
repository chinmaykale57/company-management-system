// factory/Factory.java
package com.example.sellerhelp.factory.entity;

import com.example.sellerhelp.appuser.entity.User;
import com.example.sellerhelp.centraloffice.entity.CentralOffice;
import com.example.sellerhelp.constant.ActiveStatus;
import com.example.sellerhelp.tool.entity.Bucket;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "factory")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Factory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "factory_id", nullable = false, unique = true, length = 50)
    private String factoryId;

    @Column(nullable = false, length = 255)
    private String name;

    private String city;
    private String address;

    @ManyToOne
    @JoinColumn(name = "plant_head_id")
    private User plantHead;

    @ManyToOne
    @JoinColumn(name = "central_office_id")
    private CentralOffice centralOffice;

    @Enumerated(EnumType.STRING)
    @Column(name = "is_active", nullable = false)
    private ActiveStatus isActive = ActiveStatus.ACTIVE;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "factory", cascade = CascadeType.ALL)
    private List<FactoryBay> bays = new ArrayList<>();

    @OneToMany(mappedBy = "factory")
    private List<Bucket> buckets = new ArrayList<>();
}