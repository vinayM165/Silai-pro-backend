package com.silaipro.repository;

import com.silaipro.entity.MeasurementValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MeasurementValueRepository extends JpaRepository<MeasurementValue, Long> {
}
