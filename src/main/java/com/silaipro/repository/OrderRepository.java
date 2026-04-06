package com.silaipro.repository;

import com.silaipro.entity.Order;
import com.silaipro.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByCustomerId(Long customerId);

    @Query("SELECT o FROM Order o WHERE " +
           "(:status IS NULL OR o.status = :status) AND " +
           "(:customerId IS NULL OR o.customer.id = :customerId) AND " +
           "(:search IS NULL OR LOWER(o.orderNo) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(o.customer.name) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:fromDate IS NULL OR o.deliveryDate >= :fromDate) AND " +
           "(:toDate IS NULL OR o.deliveryDate <= :toDate) AND " +
           "(:assignedTo IS NULL OR o.assignedTo.id = :assignedTo)")
    Page<Order> findAllFiltered(
            @Param("status") OrderStatus status,
            @Param("customerId") Long customerId,
            @Param("search") String search,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("assignedTo") Long assignedTo,
            Pageable pageable);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = :status")
    long countByStatus(@Param("status") OrderStatus status);

    @Query("SELECT o FROM Order o WHERE o.deliveryDate < :today AND o.status NOT IN" +
           " (com.silaipro.enums.OrderStatus.DELIVERED, com.silaipro.enums.OrderStatus.CANCELLED)")
    List<Order> findOverdueOrders(@Param("today") LocalDate today);

    @Query("SELECT o FROM Order o WHERE o.deliveryDate = :today")
    List<Order> findTodayDeliveries(@Param("today") LocalDate today);

    @Query("SELECT MAX(o.orderNo) FROM Order o WHERE o.orderNo LIKE CONCAT('ORD-', :year, '-%')")
    String findMaxOrderNoByYear(@Param("year") String year);

    // ── Reports queries ──────────────────────────────────────────────────────

    /** Count orders created in a datetime range. */
    @Query("SELECT COUNT(o) FROM Order o WHERE o.createdAt >= :from AND o.createdAt <= :to")
    long countCreatedBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    /** Count orders delivered (status=DELIVERED) with updatedAt in range. */
    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = com.silaipro.enums.OrderStatus.DELIVERED " +
           "AND o.updatedAt >= :from AND o.updatedAt <= :to")
    long countDeliveredBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    /** Orders handled by a specific staff member in a date range. */
    @Query("SELECT o FROM Order o WHERE o.assignedTo.id = :staffId AND o.createdAt >= :from AND o.createdAt <= :to")
    List<Order> findByAssignedToIdAndCreatedAtBetween(@Param("staffId") Long staffId,
                                                       @Param("from") LocalDateTime from,
                                                       @Param("to") LocalDateTime to);

    /** All distinct staff users who were assigned at least one order. */
    @Query("SELECT DISTINCT o.assignedTo FROM Order o WHERE o.assignedTo IS NOT NULL")
    List<com.silaipro.entity.User> findDistinctStaffWithOrders();
}
