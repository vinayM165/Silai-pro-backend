package com.silaipro.dto.billing;

import com.silaipro.dto.order.OrderItem;
import com.silaipro.enums.PaymentStatus;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data @Builder
public class InvoiceResponse {
    private Long id;
    private String invoiceNo;
    private Long orderId;
    private String orderNo;
    private Long customerId;
    private String customerName;
    private String customerPhone;
    private List<OrderItem> items;
    private BigDecimal subtotal;
    private BigDecimal discount;
    private BigDecimal taxRate;
    private BigDecimal taxAmount;
    private BigDecimal total;
    private BigDecimal totalPaid;
    private BigDecimal balanceDue;
    private PaymentStatus paymentStatus;
    private LocalDate invoiceDate;
    private LocalDate dueDate;
    private String notes;
    private List<PaymentResponse> payments;
}
