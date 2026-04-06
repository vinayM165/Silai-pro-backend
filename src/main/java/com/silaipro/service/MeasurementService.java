package com.silaipro.service;

import com.silaipro.dto.measurement.*;
import com.silaipro.entity.*;
import com.silaipro.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MeasurementService {

    private final MeasurementRepository measurementRepository;
    private final MeasurementValueRepository valueRepository;
    private final CustomerRepository customerRepository;
    private final MeasurementCategoryRepository categoryRepository;
    private final MeasurementFieldRepository fieldRepository;
    private final UserRepository userRepository;

    @Transactional
    public MeasurementResponse saveMeasurement(MeasurementRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));

        MeasurementCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));

        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User takenBy = userRepository.findByPhoneOrEmail(currentUsername)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Measurement measurement = Measurement.builder()
                .customer(customer)
                .category(category)
                .takenBy(takenBy)
                .takenAt(request.getTakenAt() != null ? request.getTakenAt() : LocalDateTime.now())
                .notes(request.getNotes())
                .build();

        measurement = measurementRepository.save(measurement);

        List<MeasurementValue> values = new ArrayList<>();
        for (MeasurementRequest.MeasurementValueRequest valReq : request.getValues()) {
            MeasurementField field = fieldRepository.findById(valReq.getFieldId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Field not found: " + valReq.getFieldId()));

            MeasurementValue value = MeasurementValue.builder()
                    .measurement(measurement)
                    .field(field)
                    .fieldValue(valReq.getValue())
                    .build();
            values.add(value);
        }

        valueRepository.saveAll(values);
        measurement.setValues(values);

        return mapToResponse(measurement);
    }

    public List<MeasurementResponse> getMeasurementsByCustomer(Long customerId) {
        return measurementRepository.findByCustomerId(customerId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public MeasurementResponse getMeasurementById(Long id) {
        Measurement measurement = measurementRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Measurement not found"));
        return mapToResponse(measurement);
    }

    public MeasurementResponse getLatestMeasurement(Long customerId, Long categoryId) {
        Measurement measurement = measurementRepository.findFirstByCustomerIdAndCategoryIdOrderByTakenAtDesc(customerId, categoryId);
        if (measurement == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No measurements found for this customer and category");
        }
        return mapToResponse(measurement);
    }

    public ComparisonResponse compareMeasurements(Long id1, Long id2) {
        Measurement m1 = measurementRepository.findById(id1)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Measurement 1 not found"));
        Measurement m2 = measurementRepository.findById(id2)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Measurement 2 not found"));

        if (!m1.getCategory().getId().equals(m2.getCategory().getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Categories must be identical for comparison");
        }

        MeasurementResponse r1 = mapToResponse(m1);
        MeasurementResponse r2 = mapToResponse(m2);

        Map<Long, MeasurementResponse.MeasurementValueResponse> map1 = r1.getValues().stream()
                .collect(Collectors.toMap(MeasurementResponse.MeasurementValueResponse::getFieldId, Function.identity()));
        Map<Long, MeasurementResponse.MeasurementValueResponse> map2 = r2.getValues().stream()
                .collect(Collectors.toMap(MeasurementResponse.MeasurementValueResponse::getFieldId, Function.identity()));

        List<MeasurementField> fields = fieldRepository.findByCategoryIdOrderBySortOrderAsc(m1.getCategory().getId());
        List<ComparisonResponse.FieldDifference> differences = new ArrayList<>();

        for (MeasurementField field : fields) {
            MeasurementResponse.MeasurementValueResponse v1 = map1.get(field.getId());
            MeasurementResponse.MeasurementValueResponse v2 = map2.get(field.getId());

            String val1 = v1 != null ? v1.getValue() : "N/A";
            String val2 = v2 != null ? v2.getValue() : "N/A";

            differences.add(ComparisonResponse.FieldDifference.builder()
                    .fieldId(field.getId())
                    .fieldName(field.getFieldName())
                    .value1(val1)
                    .value2(val2)
                    .unit(field.getUnit())
                    .change(calculateChange(val1, val2))
                    .build());
        }

        return ComparisonResponse.builder()
                .measurement1(r1)
                .measurement2(r2)
                .differences(differences)
                .build();
    }

    private String calculateChange(String v1, String v2) {
        try {
            double d1 = Double.parseDouble(v1);
            double d2 = Double.parseDouble(v2);
            double diff = d2 - d1;
            return (diff >= 0 ? "+" : "") + diff;
        } catch (Exception e) {
            return v1.equals(v2) ? "None" : "Changed";
        }
    }

    private MeasurementResponse mapToResponse(Measurement m) {
        return MeasurementResponse.builder()
                .id(m.getId())
                .customerId(m.getCustomer().getId())
                .customerName(m.getCustomer().getName())
                .categoryId(m.getCategory().getId())
                .categoryName(m.getCategory().getName())
                .takenByName(m.getTakenBy() != null ? m.getTakenBy().getName() : "Unknown")
                .takenAt(m.getTakenAt())
                .notes(m.getNotes())
                .values(m.getValues().stream().map(this::mapToValueResponse).collect(Collectors.toList()))
                .build();
    }

    private MeasurementResponse.MeasurementValueResponse mapToValueResponse(MeasurementValue v) {
        return MeasurementResponse.MeasurementValueResponse.builder()
                .fieldId(v.getField().getId())
                .fieldName(v.getField().getFieldName())
                .value(v.getFieldValue())
                .unit(v.getField().getUnit())
                .build();
    }
}
