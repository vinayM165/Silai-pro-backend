package com.silaipro.dto.customer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CustomerResponse {
    private Long id;
    private String name;
    private String phone;
    private String whatsapp;
    private String email;
    private String address;
    private String city;
    private String profilePhotoUrl;
    private LocalDate dateOfBirth;
    private String customerType;
    private String reference;
    private String notes;
    private LocalDate dateJoined;
    private Boolean isActive;
    private LocalDateTime createdAt;
    
    // Calculated fields for summary view
    private Double totalSpent;
    private Double pendingAmount;
    private Integer totalOrders;
    private LocalDateTime lastVisit;
}
