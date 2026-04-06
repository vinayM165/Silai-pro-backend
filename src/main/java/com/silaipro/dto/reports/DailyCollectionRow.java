package com.silaipro.dto.reports;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class DailyCollectionRow {
    private LocalDate date;
    private BigDecimal cashCollected;
    private BigDecimal upiCollected;
    private BigDecimal totalCollected;
    private long paymentCount;
}
