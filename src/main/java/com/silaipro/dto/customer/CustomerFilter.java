package com.silaipro.dto.customer;

import lombok.Data;
import java.time.LocalDate;

@Data
public class CustomerFilter {
    private String search;
    private String type;
    private String area;
    private Boolean pendingPayment;
    private LocalDate startDate;
    private LocalDate endDate;
}
