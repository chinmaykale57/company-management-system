package com.example.sellerhelp.tool.entity;

import com.example.sellerhelp.constant.Expensive;
import com.example.sellerhelp.constant.Perishable;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tools")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Tool {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tool_id", unique = true, nullable = false, updatable = false)
    private String toolId;

    @Column(nullable = false, unique = true, length = 255)
    private String name;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private ToolCategory category;

    @Column(name = "image_url")
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "is_perishable", nullable = false)
    private Perishable isPerishable = Perishable.NON_PERISHABLE;

    @Enumerated(EnumType.STRING)
    @Column(name = "is_expensive", nullable = false)
    private Expensive isExpensive = Expensive.INEXPENSIVE;

    @Column(nullable = false)
    private Long threshold = 0L;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "tool")
    private List<ToolStock> toolStocks = new ArrayList<>();
}