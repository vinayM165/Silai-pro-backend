package com.silaipro.controller;

import com.silaipro.dto.measurement.ComparisonResponse;
import com.silaipro.dto.measurement.MeasurementRequest;
import com.silaipro.dto.measurement.MeasurementResponse;
import com.silaipro.security.HasPermission;
import com.silaipro.service.MeasurementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Measurements", description = "Customer measurement management")
public class MeasurementController {

    private final MeasurementService measurementService;

    @PostMapping("/measurements")
    @HasPermission("MEASUREMENT_ADD")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Save a new measurement record")
    public MeasurementResponse saveMeasurement(@Valid @RequestBody MeasurementRequest request) {
        return measurementService.saveMeasurement(request);
    }

    @GetMapping("/customers/{id}/measurements")
    @HasPermission("MEASUREMENT_VIEW")
    @Operation(summary = "Get all measurements for a customer")
    public List<MeasurementResponse> getMeasurementsByCustomer(@PathVariable Long id) {
        return measurementService.getMeasurementsByCustomer(id);
    }

    @GetMapping("/measurements/{id}")
    @HasPermission("MEASUREMENT_VIEW")
    @Operation(summary = "Get measurement by ID")
    public MeasurementResponse getMeasurementById(@PathVariable Long id) {
        return measurementService.getMeasurementById(id);
    }

    @GetMapping("/customers/{id}/measurements/latest")
    @HasPermission("MEASUREMENT_VIEW")
    @Operation(summary = "Get latest measurement for a customer by category")
    public MeasurementResponse getLatestMeasurement(
            @PathVariable Long id,
            @RequestParam Long categoryId) {
        return measurementService.getLatestMeasurement(id, categoryId);
    }

    @GetMapping("/measurements/compare")
    @HasPermission("MEASUREMENT_VIEW")
    @Operation(summary = "Compare two measurement records")
    public ComparisonResponse compareMeasurements(
            @RequestParam Long id1,
            @RequestParam Long id2) {
        return measurementService.compareMeasurements(id1, id2);
    }
}
