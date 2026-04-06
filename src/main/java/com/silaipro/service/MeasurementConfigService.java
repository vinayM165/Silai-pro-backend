package com.silaipro.service;

import com.silaipro.dto.measurement.*;
import com.silaipro.entity.MeasurementCategory;
import com.silaipro.entity.MeasurementField;
import com.silaipro.repository.MeasurementCategoryRepository;
import com.silaipro.repository.MeasurementFieldRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MeasurementConfigService {

    private final MeasurementCategoryRepository categoryRepository;
    private final MeasurementFieldRepository fieldRepository;

    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findByIsActiveOrderBySortOrderAsc(true)
                .stream()
                .map(this::mapToCategoryResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        MeasurementCategory category = MeasurementCategory.builder()
                .name(request.getName())
                .description(request.getDescription())
                .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0)
                .isActive(true)
                .build();
        category = categoryRepository.save(category);
        return mapToCategoryResponse(category);
    }

    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        MeasurementCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));
        
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        if (request.getSortOrder() != null) {
            category.setSortOrder(request.getSortOrder());
        }
        
        category = categoryRepository.save(category);
        return mapToCategoryResponse(category);
    }

    @Transactional
    public void deleteCategory(Long id) {
        MeasurementCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));
        category.setIsActive(false);
        categoryRepository.save(category);
    }

    public List<FieldResponse> getFieldsByCategory(Long categoryId) {
        return fieldRepository.findByCategoryIdOrderBySortOrderAsc(categoryId)
                .stream()
                .map(this::mapToFieldResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public FieldResponse addField(Long categoryId, FieldRequest request) {
        MeasurementCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));

        MeasurementField field = MeasurementField.builder()
                .category(category)
                .fieldName(request.getFieldName())
                .fieldType(request.getFieldType())
                .unit(request.getUnit())
                .isRequired(request.getIsRequired() != null ? request.getIsRequired() : false)
                .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0)
                .optionsJson(request.getOptionsJson())
                .build();
        
        field = fieldRepository.save(field);
        return mapToFieldResponse(field);
    }

    @Transactional
    public FieldResponse updateField(Long fieldId, FieldRequest request) {
        MeasurementField field = fieldRepository.findById(fieldId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Field not found"));
        
        field.setFieldName(request.getFieldName());
        field.setFieldType(request.getFieldType());
        field.setUnit(request.getUnit());
        field.setIsRequired(request.getIsRequired() != null ? request.getIsRequired() : false);
        if (request.getSortOrder() != null) {
            field.setSortOrder(request.getSortOrder());
        }
        field.setOptionsJson(request.getOptionsJson());
        
        field = fieldRepository.save(field);
        return mapToFieldResponse(field);
    }

    @Transactional
    public void deleteField(Long fieldId) {
        if (!fieldRepository.existsById(fieldId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Field not found");
        }
        fieldRepository.deleteById(fieldId);
    }

    @Transactional
    public void reorderFields(Long categoryId, List<Long> orderedFieldIds) {
        List<MeasurementField> fields = fieldRepository.findByCategoryIdOrderBySortOrderAsc(categoryId);
        Map<Long, MeasurementField> fieldMap = fields.stream()
                .collect(Collectors.toMap(MeasurementField::getId, Function.identity()));
        
        for (int i = 0; i < orderedFieldIds.size(); i++) {
            Long fieldId = orderedFieldIds.get(i);
            MeasurementField field = fieldMap.get(fieldId);
            if (field != null) {
                field.setSortOrder(i);
            }
        }
        fieldRepository.saveAll(fields);
    }

    private CategoryResponse mapToCategoryResponse(MeasurementCategory category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .isActive(category.getIsActive())
                .sortOrder(category.getSortOrder())
                .createdAt(category.getCreatedAt())
                .build();
    }

    private FieldResponse mapToFieldResponse(MeasurementField field) {
        return FieldResponse.builder()
                .id(field.getId())
                .categoryId(field.getCategory().getId())
                .fieldName(field.getFieldName())
                .fieldType(field.getFieldType())
                .unit(field.getUnit())
                .isRequired(field.getIsRequired())
                .sortOrder(field.getSortOrder())
                .optionsJson(field.getOptionsJson())
                .createdAt(field.getCreatedAt())
                .build();
    }
}
