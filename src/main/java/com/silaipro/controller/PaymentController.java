package com.silaipro.controller;

import com.silaipro.dto.billing.PaymentRequest;
import com.silaipro.dto.billing.PaymentResponse;
import com.silaipro.security.HasPermission;
import com.silaipro.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Payment management endpoints")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @HasPermission("PAYMENT_CREATE")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Record a new payment")
    public PaymentResponse recordPayment(@Valid @RequestBody PaymentRequest request) {
        return paymentService.recordPayment(request);
    }

    @GetMapping("/{id}")
    @HasPermission("PAYMENT_VIEW")
    @Operation(summary = "Get payment by ID")
    public PaymentResponse getPaymentById(@PathVariable Long id) {
        return paymentService.getPaymentById(id);
    }

    @GetMapping("/invoice/{invoiceId}")
    @HasPermission("PAYMENT_VIEW")
    @Operation(summary = "Get all payments for an invoice")
    public List<PaymentResponse> getPaymentsByInvoice(@PathVariable Long invoiceId) {
        return paymentService.getPaymentsByInvoice(invoiceId);
    }
}
