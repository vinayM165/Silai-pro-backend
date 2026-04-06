package com.silaipro.controller;

import com.silaipro.dto.measurement.CategoryRequest;
import com.silaipro.dto.measurement.CategoryResponse;
import com.silaipro.dto.measurement.FieldRequest;
import com.silaipro.dto.measurement.FieldResponse;
import com.silaipro.security.HasPermission;
import com.silaipro.service.MeasurementConfigService;
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
@Tag(name = "Measurements", description = "Endpoints for configuring measurement categories and fields")
public class MeasurementConfigController {

    private final MeasurementConfigService configService;

    // Categories Endpoints
    @GetMapping("/measurement-categories")
    @HasPermission("VIEW_CUSTOMER")
    @Operation(summary = "Get all measurement categories")
    public List<CategoryResponse> getAllCategories() {
        return configService.getAllCategories();
    }

    @PostMapping("/measurement-categories")
    @HasPermission("MANAGE_MEASUREMENTS")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new measurement category")
    public CategoryResponse createCategory(@Valid @RequestBody CategoryRequest request) {
        return configService.createCategory(request);
    }

    @PutMapping("/measurement-categories/{id}")
    @HasPermission("MANAGE_MEASUREMENTS")
    @Operation(summary = "Update an existing measurement category")
    public CategoryResponse updateCategory(@PathVariable Long id, @Valid @RequestBody CategoryRequest request) {
        return configService.updateCategory(id, request);
    }

    @DeleteMapping("/measurement-categories/{id}")
    @HasPermission("MANAGE_MEASUREMENTS")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a measurement category")
    public void deleteCategory(@PathVariable Long id) {
        configService.deleteCategory(id);
    }

    @GetMapping("/measurement-categories/{id}/fields")
    @HasPermission("VIEW_CUSTOMER")
    @Operation(summary = "Get all fields for a specific category")
    public List<FieldResponse> getFieldsByCategory(@PathVariable Long id) {
        return configService.getFieldsByCategory(id);
    }

    // Fields Endpoints
    @PostMapping("/measurement-fields")
    @HasPermission("MANAGE_MEASUREMENTS")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add a field to a category")
    public FieldResponse createField(@RequestParam Long categoryId, @Valid @RequestBody FieldRequest request) {
        return configService.addField(categoryId, request);
    }

    @PutMapping("/measurement-fields/{id}")
    @HasPermission("MANAGE_MEASUREMENTS")
    @Operation(summary = "Update a measurement field")
    public FieldResponse updateField(@PathVariable Long id, @Valid @RequestBody FieldRequest request) {
        return configService.updateField(id, request);
    }

    @DeleteMapping("/measurement-fields/{id}")
    @HasPermission("MANAGE_MEASUREMENTS")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a measurement field")
    public void deleteField(@PathVariable Long id) {
        configService.deleteField(id);
    }

    @PutMapping("/measurement-fields/reorder")
    @HasPermission("MANAGE_MEASUREMENTS")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Reorder fields within a category")
    public void reorderFields(@RequestParam Long categoryId, @RequestBody List<Long> orderedFieldIds) {
        configService.reorderFields(categoryId, orderedFieldIds);
    }
}
