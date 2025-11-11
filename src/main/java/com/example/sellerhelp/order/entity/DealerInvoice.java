package com.example.sellerhelp.order.entity;

import com.example.sellerhelp.appuser.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@Entity
@Table(name = "dealer_invoice")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DealerInvoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "invoice_id", nullable = false, unique = true, length = 50)
    private String invoiceId;

    @ManyToOne
    @JoinColumn(name = "dealer_id", nullable = false)
    private User dealer;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private DealerOrder order;
    private String url;

    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}