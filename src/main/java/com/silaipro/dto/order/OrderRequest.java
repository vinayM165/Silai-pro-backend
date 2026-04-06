package com.silaipro.dto.order;

import com.silaipro.enums.OrderPriority;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class OrderRequest {
    @NotNull(message = "Customer ID is required")
    private Long customerId;

    @NotEmpty(message = "Order must have at least one item")
    @Valid
    private List<OrderItem> items;

    private BigDecimal advancePaid;

    @NotNull(message = "Expected delivery date is required")
    private LocalDate expectedDelivery;

    private OrderPriority priority;
    private String specialInstructions;
    private LocalDate trialDate;
    private Long assignedTo;
}
