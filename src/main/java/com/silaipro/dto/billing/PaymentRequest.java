package com.silaipro.dto.billing;

import com.silaipro.enums.PaymentMode;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PaymentRequest {
    @NotNull(message = "Invoice ID is required")
    private Long invoiceId;
    
    @NotNull(message = "Amount is required")
    private BigDecimal amount;
    
    @NotNull(message = "Payment mode is required")
    private PaymentMode mode;
    
    @NotNull(message = "Payment date is required")
    private LocalDate paymentDate;
    
    private String receiptNo;
    private String notes;
}
