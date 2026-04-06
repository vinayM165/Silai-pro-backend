package com.silaipro.dto.billing;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class InvoiceRequest {
    @NotNull(message = "Order ID is required")
    private Long orderId;
    
    private BigDecimal discount;
    private BigDecimal taxRate;
    private LocalDate dueDate;
    private String notes;
}
