package com.silaipro.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class StaffWorkReport {
    private Long staffId;
    private String staffName;
    private long ordersHandled;
    private long deliveredOrders;
    private BigDecimal revenueGenerated;
    private BigDecimal totalCollected;
}
