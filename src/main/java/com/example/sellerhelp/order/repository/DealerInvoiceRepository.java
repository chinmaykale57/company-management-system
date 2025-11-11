package com.example.sellerhelp.order.repository;

import com.example.sellerhelp.order.entity.DealerInvoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DealerInvoiceRepository extends JpaRepository<DealerInvoice, Long> {
    Optional<DealerInvoice> findByInvoiceId(String invoiceId);
}