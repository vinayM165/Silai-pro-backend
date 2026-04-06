package com.silaipro.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "measurement_values")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class MeasurementValue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "measurement_id", nullable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Measurement measurement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "field_id", nullable = false)
    private MeasurementField field;

    @Column(name = "field_value")
    private String fieldValue;
}
