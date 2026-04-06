package com.silaipro.repository;

import com.silaipro.entity.Payment;
import com.silaipro.enums.PaymentMode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByInvoiceOrderCustomerId(Long customerId);

    List<Payment> findByInvoiceId(Long invoiceId);

    // ── Reports queries ──────────────────────────────────────────────────────

    /** All payments in a date range. */
    List<Payment> findByPaymentDateBetweenOrderByPaymentDateAsc(LocalDate from, LocalDate to);

    /** Sum of all payments in a date range. */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.paymentDate >= :from AND p.paymentDate <= :to")
    BigDecimal sumPaymentsBetween(@Param("from") LocalDate from, @Param("to") LocalDate to);

    /** Sum of payments by mode in a date range. */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.paymentDate >= :from AND p.paymentDate <= :to AND p.mode = :mode")
    BigDecimal sumPaymentsByModeBetween(@Param("from") LocalDate from, @Param("to") LocalDate to, @Param("mode") PaymentMode mode);

    /** Payments received by a specific staff member in a date range. */
    @Query("SELECT p FROM Payment p WHERE p.receivedBy.id = :staffId AND p.paymentDate >= :from AND p.paymentDate <= :to")
    List<Payment> findByReceivedByIdAndDateRange(@Param("staffId") Long staffId,
                                                  @Param("from") LocalDate from,
                                                  @Param("to") LocalDate to);

    /** Collections per month for a given year. */
    @Query("SELECT MONTH(p.paymentDate), COALESCE(SUM(p.amount), 0) FROM Payment p " +
           "WHERE YEAR(p.paymentDate) = :year GROUP BY MONTH(p.paymentDate)")
    List<Object[]> collectionsByMonth(@Param("year") int year);
}
