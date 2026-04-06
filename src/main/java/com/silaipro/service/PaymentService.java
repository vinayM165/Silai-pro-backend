package com.silaipro.service;

import com.silaipro.dto.billing.PaymentRequest;
import com.silaipro.dto.billing.PaymentResponse;
import com.silaipro.entity.Invoice;
import com.silaipro.entity.Payment;
import com.silaipro.entity.User;
import com.silaipro.repository.InvoiceRepository;
import com.silaipro.repository.PaymentRepository;
import com.silaipro.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final UserRepository userRepository;
    private final InvoiceService invoiceService;
    private final AuditLogService auditLogService;

    @Transactional
    public PaymentResponse recordPayment(PaymentRequest request) {
        Invoice invoice = invoiceRepository.findById(request.getInvoiceId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invoice not found"));

        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User receivedBy = userRepository.findByPhoneOrEmail(currentUsername)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Payment payment = Payment.builder()
                .invoice(invoice)
                .amount(request.getAmount())
                .mode(request.getMode())
                .paymentDate(request.getPaymentDate() != null ? request.getPaymentDate() : LocalDate.now())
                .receivedBy(receivedBy)
                .receiptNo(request.getReceiptNo())
                .notes(request.getNotes())
                .build();

        payment = paymentRepository.save(payment);
        
        auditLogService.log("PAYMENT_RECORD", "INVOICE", invoice.getId(), null, payment.getAmount().toString());
        
        // Trigger invoice status update
        invoiceService.updatePaymentStatus(invoice.getId());

        return mapToResponse(payment);
    }

    public List<PaymentResponse> getPaymentsByInvoice(Long invoiceId) {
        return paymentRepository.findByInvoiceId(invoiceId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public PaymentResponse getPaymentById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found"));
        return mapToResponse(payment);
    }

    private PaymentResponse mapToResponse(Payment p) {
        return PaymentResponse.builder()
                .id(p.getId())
                .invoiceId(p.getInvoice().getId())
                .invoiceNo(p.getInvoice().getInvoiceNo())
                .amount(p.getAmount())
                .mode(p.getMode())
                .paymentDate(p.getPaymentDate())
                .receiptNo(p.getReceiptNo())
                .notes(p.getNotes())
                .receivedByName(p.getReceivedBy() != null ? p.getReceivedBy().getName() : "Unknown")
                .build();
    }
}
