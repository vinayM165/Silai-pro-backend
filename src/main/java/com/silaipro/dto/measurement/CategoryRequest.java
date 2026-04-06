package com.silaipro.dto.measurement;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CategoryRequest {
    @NotBlank(message = "Category name is required")
    private String name;
    private String description;
    private Integer sortOrder;
}
