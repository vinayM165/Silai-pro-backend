package com.silaipro.dto.billing;

import com.silaipro.enums.PaymentMode;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data @Builder
public class PaymentResponse {
    private Long id;
    private Long invoiceId;
    private String invoiceNo;
    private BigDecimal amount;
    private PaymentMode mode;
    private LocalDate paymentDate;
    private String receiptNo;
    private String notes;
    private String receivedByName;
}
