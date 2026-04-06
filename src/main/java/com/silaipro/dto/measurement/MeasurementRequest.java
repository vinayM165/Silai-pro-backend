package com.silaipro.dto.measurement;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class MeasurementRequest {
    @NotNull(message = "Customer ID is required")
    private Long customerId;

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    private LocalDateTime takenAt;
    private String notes;

    @NotNull(message = "Measurement values are required")
    private List<MeasurementValueRequest> values;

    @Data
    public static class MeasurementValueRequest {
        @NotNull(message = "Field ID is required")
        private Long fieldId;
        
        @NotNull(message = "Field value is required")
        private String value;
    }
}
