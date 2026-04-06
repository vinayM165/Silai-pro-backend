package com.silaipro.repository;

import com.silaipro.entity.Measurement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MeasurementRepository extends JpaRepository<Measurement, Long> {
    List<Measurement> findByCustomerId(Long customerId);
    Measurement findFirstByCustomerIdAndCategoryIdOrderByTakenAtDesc(Long customerId, Long categoryId);
}
