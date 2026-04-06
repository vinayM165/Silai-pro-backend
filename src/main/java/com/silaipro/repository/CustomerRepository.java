package com.silaipro.repository;

import com.silaipro.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    @Query("SELECT c, COUNT(o.id) as totalOrders, " +
           "SUM(o.totalAmount - o.advancePaid) as pendingAmount, " +
           "MAX(o.createdAt) as lastVisit " +
           "FROM Customer c " +
           "LEFT JOIN Order o ON o.customer = c " +
           "LEFT JOIN Invoice i ON i.order = o " +
           "WHERE c.isActive = true " +
           "AND (:search IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR c.phone LIKE CONCAT('%', :search, '%') " +
           "OR CAST(c.id AS string) LIKE CONCAT('%', :search, '%')) " +
           "AND (:type IS NULL OR c.customerType = :type) " +
           "AND (:area IS NULL OR c.city = :area) " +
           "AND (:pendingPayment IS NULL OR (:pendingPayment = true AND i.paymentStatus <> 'PAID') " +
           "OR (:pendingPayment = false AND i.paymentStatus = 'PAID')) " +
           "AND (:startDate IS NULL OR c.dateJoined >= :startDate) " +
           "AND (:endDate IS NULL OR c.dateJoined <= :endDate) " +
           "GROUP BY c.id")
    Page<Object[]> searchCustomers(
            @Param("search") String search,
            @Param("type") String type,
            @Param("area") String area,
            @Param("pendingPayment") Boolean pendingPayment,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable);
}
