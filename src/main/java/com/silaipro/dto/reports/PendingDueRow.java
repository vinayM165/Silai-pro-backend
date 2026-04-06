package com.silaipro.dto.reports;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PendingDueRow {
    private Long customerId;
    private String customerName;
    private String phone;
    private Long invoiceId;
    private String invoiceNo;
    private BigDecimal invoiceTotal;
    private BigDecimal totalPaid;
    private BigDecimal balanceDue;
    private long daysPending;         // days since invoice date
    private LocalDate lastPaymentDate;
}
