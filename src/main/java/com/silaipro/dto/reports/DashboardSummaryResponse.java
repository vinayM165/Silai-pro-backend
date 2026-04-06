package com.silaipro.dto.reports;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class DashboardSummaryResponse {
    private BigDecimal totalRevenue;      // sum of invoice totals
    private BigDecimal amountReceived;    // sum of payments
    private BigDecimal pendingDues;       // sum of balanceDue
    private long ordersCreated;
    private long ordersDelivered;
}
