package com.silaipro.entity;

import com.silaipro.enums.FieldType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "measurement_fields")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class MeasurementField {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private MeasurementCategory category;

    @Column(nullable = false)
    private String fieldName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FieldType fieldType;

    private String unit;

    @Builder.Default
    private Boolean isRequired = false;

    @Builder.Default
    private Integer sortOrder = 0;

    @Column(columnDefinition = "TEXT")
    private String optionsJson;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
