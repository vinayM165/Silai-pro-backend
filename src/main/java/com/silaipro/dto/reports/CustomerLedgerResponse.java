package com.silaipro.dto.reports;

import com.silaipro.dto.billing.InvoiceResponse;
import com.silaipro.dto.customer.CustomerResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CustomerLedgerResponse {
    private CustomerResponse customer;
    private List<InvoiceResponse> invoices;   // each invoice contains its payments timeline
}
