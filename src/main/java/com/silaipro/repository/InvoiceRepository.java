package com.silaipro.repository;

import com.silaipro.entity.Invoice;
import com.silaipro.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    List<Invoice> findByOrderCustomerId(Long customerId);

    Optional<Invoice> findByOrderId(Long orderId);

    @Query("SELECT MAX(i.invoiceNo) FROM Invoice i WHERE i.invoiceNo LIKE CONCAT('INV-', :year, '-%')")
    String findMaxInvoiceNoByYear(@Param("year") String year);

    // ── Reports queries ──────────────────────────────────────────────────────

    /** Sum of invoice totals in a date range (by invoiceDate). */
    @Query("SELECT COALESCE(SUM(i.total), 0) FROM Invoice i WHERE i.invoiceDate >= :from AND i.invoiceDate <= :to")
    BigDecimal sumRevenueBetween(@Param("from") LocalDate from, @Param("to") LocalDate to);

    /** Count orders created in a date range (using invoice date as proxy). */
    @Query("SELECT COUNT(i) FROM Invoice i WHERE i.invoiceDate >= :from AND i.invoiceDate <= :to")
    long countInvoicesBetween(@Param("from") LocalDate from, @Param("to") LocalDate to);

    /** Count orders delivered in a date range. */
    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = com.silaipro.enums.OrderStatus.DELIVERED " +
           "AND o.updatedAt >= :fromDt AND o.updatedAt <= :toDt")
    long countDeliveredBetween(@Param("fromDt") java.time.LocalDateTime fromDt,
                               @Param("toDt") java.time.LocalDateTime toDt);

    /** All invoices with a balance still outstanding (not PAID). */
    @Query("SELECT i FROM Invoice i WHERE i.paymentStatus <> com.silaipro.enums.PaymentStatus.PAID")
    List<Invoice> findAllWithPendingBalance();

    /** Overdue: status=OVERDUE or (dueDate < today and NOT PAID). */
    @Query("SELECT i FROM Invoice i WHERE i.paymentStatus = com.silaipro.enums.PaymentStatus.OVERDUE " +
           "OR (i.dueDate < :today AND i.paymentStatus <> com.silaipro.enums.PaymentStatus.PAID)")
    List<Invoice> findOverdueInvoices(@Param("today") LocalDate today);

    /** Revenue per month for a given year. */
    @Query("SELECT MONTH(i.invoiceDate), COALESCE(SUM(i.total), 0) FROM Invoice i " +
           "WHERE YEAR(i.invoiceDate) = :year GROUP BY MONTH(i.invoiceDate)")
    List<Object[]> revenueByMonth(@Param("year") int year);
}
