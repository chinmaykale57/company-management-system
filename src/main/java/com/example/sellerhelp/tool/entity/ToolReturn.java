package com.example.sellerhelp.tool.entity;

import com.example.sellerhelp.appuser.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "tool_return")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ToolReturn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // THIS IS THE FIX: It should be ManyToOne
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tool_issuance_id", nullable = false)
    private ToolIssuance toolIssuance;

    @Column(name = "fit_quantity", nullable = false)
    private Long fitQuantity = 0L;

    @Column(name = "unfit_quantity", nullable = false)
    private Long unfitQuantity = 0L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private User updatedBy;

    @Column(name = "returned_at", nullable = false)
    private LocalDateTime returnedAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}