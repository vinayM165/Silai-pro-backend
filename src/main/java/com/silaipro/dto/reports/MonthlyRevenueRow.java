package com.silaipro.dto.reports;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class MonthlyRevenueRow {
    private int month;              // 1–12
    private String monthName;       // e.g., "January"
    private BigDecimal revenue;     // sum of invoice totals
    private BigDecimal collections; // sum of payments received
    private BigDecimal pendingDues; // revenue - collections
}
