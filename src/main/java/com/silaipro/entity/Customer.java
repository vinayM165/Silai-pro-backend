package com.silaipro.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "customers")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String phone;

    private String whatsapp;
    private String email;

    @Column(columnDefinition = "TEXT")
    private String address;

    private String city;
    private String profilePhotoUrl;
    private LocalDate dateOfBirth;
    private String customerType;
    private String reference;

    @Column(columnDefinition = "TEXT")
    private String notes;

    private LocalDate dateJoined;

    @Builder.Default
    private Boolean isActive = true;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
