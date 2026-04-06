package com.silaipro.dto.measurement;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ComparisonResponse {
    private MeasurementResponse measurement1;
    private MeasurementResponse measurement2;
    private List<FieldDifference> differences;

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class FieldDifference {
        private Long fieldId;
        private String fieldName;
        private String value1;
        private String value2;
        private String unit;
        private String change; // E.g., "+2", "-1", or "N/A" for non-numeric
    }
}
