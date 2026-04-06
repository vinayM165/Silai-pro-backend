package com.silaipro.dto.order;

import com.silaipro.enums.OrderPriority;
import com.silaipro.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class OrderResponse {
    private Long id;
    private String orderNo;
    private Long customerId;
    private String customerName;
    private List<OrderItem> items;
    private BigDecimal totalAmount;
    private BigDecimal advancePaid;
    private OrderStatus status;
    private LocalDate deliveryDate;
    private OrderPriority priority;
    private String specialInstructions;
    private LocalDate trialDate;
    private Long assignedTo;
    private String assignedToName;
    private Long createdBy;
    private String createdByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
