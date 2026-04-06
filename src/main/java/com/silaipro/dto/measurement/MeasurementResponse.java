package com.silaipro.dto.measurement;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class MeasurementResponse {
    private Long id;
    private Long customerId;
    private String customerName;
    private Long categoryId;
    private String categoryName;
    private String takenByName;
    private LocalDateTime takenAt;
    private String notes;
    private List<MeasurementValueResponse> values;

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class MeasurementValueResponse {
        private Long fieldId;
        private String fieldName;
        private String value;
        private String unit;
    }
}
