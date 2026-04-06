package com.silaipro.dto.order;

import com.silaipro.enums.OrderStatus;
import lombok.Data;
import java.time.LocalDate;

@Data
public class OrderFilter {
    private String status;
    private Long customerId;
    private String search;
    private LocalDate from;
    private LocalDate to;
    private Long assignedTo;
}
