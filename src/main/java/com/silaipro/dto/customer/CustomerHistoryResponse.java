package com.silaipro.dto.customer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CustomerHistoryResponse {
    private CustomerResponse customer;
    private List<OrderSummary> orders;
    private List<MeasurementSummary> measurements;
    private List<PaymentSummary> payments;
    private List<MessageSummary> messages;
    
    // Summary Fields
    private Double totalSpent;
    private Double totalPending;
    private Integer totalOrdersCount;

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class OrderSummary {
        private Long id;
        private String orderNumber;
        private java.time.LocalDateTime orderDate;
        private String status;
        private Double totalAmount;
        private Double paidAmount;
        private Double pendingAmount;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class MeasurementSummary {
        private Long id;
        private String categoryName;
        private java.time.LocalDateTime measuredAt;
        private String notes;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class PaymentSummary {
        private Long id;
        private java.time.LocalDateTime paymentDate;
        private Double amount;
        private String paymentMethod;
        private String referenceNo;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class MessageSummary {
        private Long id;
        private java.time.LocalDateTime sentAt;
        private String templateName;
        private String status;
    }
}
