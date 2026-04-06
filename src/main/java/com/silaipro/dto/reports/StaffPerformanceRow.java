package com.silaipro.dto.reports;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class StaffPerformanceRow {
    private Long staffId;
    private String name;
    private long ordersHandled;
    private long collectionsRecorded;
    private BigDecimal totalCollected;
}
