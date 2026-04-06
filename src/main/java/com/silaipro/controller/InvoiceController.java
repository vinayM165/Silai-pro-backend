package com.silaipro.controller;

import com.silaipro.dto.billing.InvoiceRequest;
import com.silaipro.dto.billing.InvoiceResponse;
import com.silaipro.security.HasPermission;
import com.silaipro.service.InvoiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
@Tag(name = "Billing", description = "Invoice management endpoints")
public class InvoiceController {

    private final InvoiceService invoiceService;

    @PostMapping
    @HasPermission("BILLING_CREATE")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Generate a new invoice for an order")
    public InvoiceResponse generateInvoice(@Valid @RequestBody InvoiceRequest request) {
        return invoiceService.generateInvoice(request);
    }

    @GetMapping("/{id}")
    @HasPermission("BILLING_VIEW")
    @Operation(summary = "Get invoice by ID")
    public InvoiceResponse getInvoiceById(@PathVariable Long id) {
        return invoiceService.getInvoiceById(id);
    }

    @GetMapping("/order/{orderId}")
    @HasPermission("BILLING_VIEW")
    @Operation(summary = "Get invoice by order ID")
    public InvoiceResponse getInvoiceByOrder(@PathVariable Long orderId) {
        return invoiceService.getInvoiceByOrder(orderId);
    }
}
