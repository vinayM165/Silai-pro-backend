package com.silaipro.dto.measurement;

import com.silaipro.enums.FieldType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FieldRequest {
    @NotBlank(message = "Field name is required")
    private String fieldName;

    @NotNull(message = "Field type is required")
    private FieldType fieldType;

    private String unit;
    private Boolean isRequired;
    private Integer sortOrder;
    private String optionsJson;
}
