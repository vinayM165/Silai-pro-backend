package com.silaipro.repository;

import com.silaipro.entity.MeasurementCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MeasurementCategoryRepository extends JpaRepository<MeasurementCategory, Long> {
    List<MeasurementCategory> findByIsActiveOrderBySortOrderAsc(Boolean isActive);
}
