package com.silaipro.dto.measurement;

import com.silaipro.enums.FieldType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class FieldResponse {
    private Long id;
    private Long categoryId;
    private String fieldName;
    private FieldType fieldType;
    private String unit;
    private Boolean isRequired;
    private Integer sortOrder;
    private String optionsJson;
    private LocalDateTime createdAt;
}
