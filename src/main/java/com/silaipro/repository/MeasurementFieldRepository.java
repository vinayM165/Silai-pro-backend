package com.silaipro.repository;

import com.silaipro.entity.MeasurementField;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MeasurementFieldRepository extends JpaRepository<MeasurementField, Long> {
    List<MeasurementField> findByCategoryIdOrderBySortOrderAsc(Long categoryId);
}
