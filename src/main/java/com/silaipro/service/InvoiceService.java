package com.silaipro.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.silaipro.dto.billing.*;
import com.silaipro.dto.order.OrderItem;
import com.silaipro.entity.*;
import com.silaipro.enums.PaymentStatus;
import com.silaipro.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public InvoiceResponse generateInvoice(InvoiceRequest request) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));

        if (invoiceRepository.findByOrderId(request.getOrderId()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invoice already exists for this order");
        }

        BigDecimal subtotal = order.getTotalAmount();
        BigDecimal discount = request.getDiscount() != null ? request.getDiscount() : BigDecimal.ZERO;
        BigDecimal taxRate = request.getTaxRate() != null ? request.getTaxRate() : BigDecimal.ZERO;
        
        BigDecimal taxableAmount = subtotal.subtract(discount);
        BigDecimal taxAmount = taxableAmount.multiply(taxRate).divide(BigDecimal.valueOf(100), RoundingMode.HALF_UP);
        BigDecimal total = taxableAmount.add(taxAmount);

        Invoice invoice = Invoice.builder()
                .order(order)
                .invoiceNo(generateInvoiceNo())
                .subtotal(subtotal)
                .discount(discount)
                .taxRate(taxRate)
                .taxAmount(taxAmount)
                .total(total)
                .paymentStatus(PaymentStatus.PENDING)
                .invoiceDate(LocalDate.now())
                .dueDate(request.getDueDate() != null ? request.getDueDate() : LocalDate.now().plusDays(7))
                .notes(request.getNotes())
                .build();

        invoice = invoiceRepository.save(invoice);
        return mapToResponse(invoice);
    }

    public InvoiceResponse getInvoiceByOrder(Long orderId) {
        Invoice invoice = invoiceRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invoice not found for this order"));
        return mapToResponse(invoice);
    }

    public InvoiceResponse getInvoiceById(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invoice not found"));
        return mapToResponse(invoice);
    }

    @Transactional
    public void updatePaymentStatus(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invoice not found"));

        List<Payment> payments = paymentRepository.findByInvoiceId(invoiceId);
        BigDecimal totalPaid = payments.stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalPaid.compareTo(BigDecimal.ZERO) == 0) {
            invoice.setPaymentStatus(PaymentStatus.PENDING);
        } else if (totalPaid.compareTo(invoice.getTotal()) < 0) {
            invoice.setPaymentStatus(PaymentStatus.PARTIAL);
        } else {
            invoice.setPaymentStatus(PaymentStatus.PAID);
        }

        // Check for overdue if not paid
        if (invoice.getPaymentStatus() != PaymentStatus.PAID && LocalDate.now().isAfter(invoice.getDueDate())) {
            invoice.setPaymentStatus(PaymentStatus.OVERDUE);
        }

        invoiceRepository.save(invoice);
    }

    private String generateInvoiceNo() {
        String year = String.valueOf(LocalDate.now().getYear());
        String maxInvoiceNo = invoiceRepository.findMaxInvoiceNoByYear(year);
        int seq = 1;

        if (maxInvoiceNo != null) {
            String seqStr = maxInvoiceNo.substring(maxInvoiceNo.lastIndexOf("-") + 1);
            seq = Integer.parseInt(seqStr) + 1;
        }

        return String.format("INV-%s-%03d", year, seq);
    }

    public InvoiceResponse mapToResponse(Invoice invoice) {
        List<Payment> payments = paymentRepository.findByInvoiceId(invoice.getId());
        BigDecimal totalPaid = payments.stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return InvoiceResponse.builder()
                .id(invoice.getId())
                .invoiceNo(invoice.getInvoiceNo())
                .orderId(invoice.getOrder().getId())
                .orderNo(invoice.getOrder().getOrderNo())
                .customerId(invoice.getOrder().getCustomer().getId())
                .customerName(invoice.getOrder().getCustomer().getName())
                .customerPhone(invoice.getOrder().getCustomer().getPhone())
                .items(deserializeItems(invoice.getOrder().getItemsJson()))
                .subtotal(invoice.getSubtotal())
                .discount(invoice.getDiscount())
                .taxRate(invoice.getTaxRate())
                .taxAmount(invoice.getTaxAmount())
                .total(invoice.getTotal())
                .totalPaid(totalPaid)
                .balanceDue(invoice.getTotal().subtract(totalPaid))
                .paymentStatus(invoice.getPaymentStatus())
                .invoiceDate(invoice.getInvoiceDate())
                .dueDate(invoice.getDueDate())
                .notes(invoice.getNotes())
                .payments(payments.stream().map(this::mapToPaymentResponse).collect(Collectors.toList()))
                .build();
    }

    private PaymentResponse mapToPaymentResponse(Payment p) {
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

    private List<OrderItem> deserializeItems(String json) {
        try {
            return Arrays.asList(objectMapper.readValue(json, OrderItem[].class));
        } catch (JsonProcessingException e) {
            return Collections.emptyList();
        }
    }
}
