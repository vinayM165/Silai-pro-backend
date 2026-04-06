package com.silaipro.service;

import com.silaipro.dto.customer.*;
import com.silaipro.entity.*;
import com.silaipro.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;
    private final InvoiceRepository invoiceRepository;
    private final MeasurementRepository measurementRepository;
    private final PaymentRepository paymentRepository;
    private final MessageSentRepository messageSentRepository;
    private final AuditLogService auditLogService;

    @Transactional
    public CustomerResponse createCustomer(CustomerRequest request) {
        Customer customer = Customer.builder()
                .name(request.getName())
                .phone(request.getPhone())
                .whatsapp(request.getWhatsapp())
                .email(request.getEmail())
                .address(request.getAddress())
                .city(request.getCity())
                .dateOfBirth(request.getDateOfBirth())
                .customerType(request.getCustomerType())
                .reference(request.getReference())
                .notes(request.getNotes())
                .dateJoined(LocalDate.now())
                .isActive(true)
                .build();

        customer = customerRepository.save(customer);
        return mapToResponse(customer);
    }

    @Transactional
    public CustomerResponse updateCustomer(Long id, CustomerRequest request) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));

        customer.setName(request.getName());
        customer.setPhone(request.getPhone());
        customer.setWhatsapp(request.getWhatsapp());
        customer.setEmail(request.getEmail());
        customer.setAddress(request.getAddress());
        customer.setCity(request.getCity());
        customer.setDateOfBirth(request.getDateOfBirth());
        customer.setCustomerType(request.getCustomerType());
        customer.setReference(request.getReference());
        customer.setNotes(request.getNotes());

        customer = customerRepository.save(customer);
        return mapToResponse(customer);
    }

    @Transactional
    public void deleteCustomer(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));

        customer.setIsActive(false);
        customerRepository.save(customer);

        auditLogService.log("DELETE_CUSTOMER", "CUSTOMER", id, "active", "inactive");
    }

    public CustomerResponse getCustomerById(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));
        return mapToResponse(customer);
    }

    public Page<CustomerResponse> searchCustomers(CustomerFilter filter, Pageable pageable) {
        return customerRepository.searchCustomers(
                filter.getSearch(),
                filter.getType(),
                filter.getArea(),
                filter.getPendingPayment(),
                filter.getStartDate(),
                filter.getEndDate(),
                pageable).map(this::mapToResponseWithStatsFromObject);
    }

    public CustomerHistoryResponse getCustomerHistory(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));

        List<Order> orders = orderRepository.findByCustomerId(id);
        List<Measurement> measurements = measurementRepository.findByCustomerId(id);
        List<Payment> payments = paymentRepository.findByInvoiceOrderCustomerId(id);
        List<MessageSent> messages = messageSentRepository.findByCustomerId(id);

        double totalAmount = orders.stream().mapToDouble(o -> o.getTotalAmount().doubleValue()).sum();
        double paidAmount = orders.stream().mapToDouble(o -> o.getAdvancePaid().doubleValue()).sum();

        return CustomerHistoryResponse.builder()
                .customer(mapToResponseWithStats(customer))
                .orders(orders.stream().map(this::mapToOrderSummary).collect(Collectors.toList()))
                .measurements(measurements.stream().map(this::mapToMeasurementSummary).collect(Collectors.toList()))
                .payments(payments.stream().map(this::mapToPaymentSummary).collect(Collectors.toList()))
                .messages(messages.stream().map(this::mapToMessageSummary).collect(Collectors.toList()))
                .totalSpent(totalAmount)
                .totalPending(totalAmount - paidAmount)
                .totalOrdersCount(orders.size())
                .build();
    }

    private CustomerResponse mapToResponse(Customer customer) {
        return CustomerResponse.builder()
                .id(customer.getId())
                .name(customer.getName())
                .phone(customer.getPhone())
                .whatsapp(customer.getWhatsapp())
                .email(customer.getEmail())
                .address(customer.getAddress())
                .city(customer.getCity())
                .profilePhotoUrl(customer.getProfilePhotoUrl())
                .dateOfBirth(customer.getDateOfBirth())
                .customerType(customer.getCustomerType())
                .reference(customer.getReference())
                .notes(customer.getNotes())
                .dateJoined(customer.getDateJoined())
                .isActive(customer.getIsActive())
                .createdAt(customer.getCreatedAt())
                .build();
    }

    private CustomerResponse mapToResponseWithStats(Customer customer) {
        CustomerResponse response = mapToResponse(customer);
        
        List<Order> orders = orderRepository.findByCustomerId(customer.getId());
        double totalSpent = orders.stream().mapToDouble(o -> o.getTotalAmount().doubleValue()).sum();
        double advancePaid = orders.stream().mapToDouble(o -> o.getAdvancePaid().doubleValue()).sum();
        
        response.setTotalSpent(totalSpent);
        response.setTotalOrders(orders.size());
        response.setPendingAmount(totalSpent - advancePaid);
        
        orders.stream()
                .map(Order::getCreatedAt)
                .max(LocalDateTime::compareTo)
                .ifPresent(response::setLastVisit);
                
        return response;
    }

    private CustomerResponse mapToResponseWithStatsFromObject(Object[] result) {
        Customer customer = (Customer) result[0];
        Integer totalOrders = ((Long) result[1]).intValue();
        Double pendingAmount = result[2] != null ? ((BigDecimal) result[2]).doubleValue() : 0.0;
        LocalDateTime lastVisit = (LocalDateTime) result[3];

        CustomerResponse response = mapToResponse(customer);
        response.setTotalOrders(totalOrders);
        response.setPendingAmount(pendingAmount);
        response.setLastVisit(lastVisit);
        
        // Total spent isn't in search result, but can be added if needed
        // For search view usually pending and total orders are enough
        return response;
    }

    private CustomerHistoryResponse.OrderSummary mapToOrderSummary(Order order) {
        return CustomerHistoryResponse.OrderSummary.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNo())
                .orderDate(order.getCreatedAt())
                .status(order.getStatus().toString())
                .totalAmount(order.getTotalAmount().doubleValue())
                .paidAmount(order.getAdvancePaid().doubleValue())
                .pendingAmount(order.getTotalAmount().subtract(order.getAdvancePaid()).doubleValue())
                .build();
    }

    private CustomerHistoryResponse.MeasurementSummary mapToMeasurementSummary(Measurement m) {
        return CustomerHistoryResponse.MeasurementSummary.builder()
                .id(m.getId())
                .categoryName(m.getCategory() != null ? m.getCategory().getName() : "Unknown")
                .measuredAt(m.getTakenAt())
                .notes(m.getNotes())
                .build();
    }

    private CustomerHistoryResponse.PaymentSummary mapToPaymentSummary(Payment p) {
        return CustomerHistoryResponse.PaymentSummary.builder()
                .id(p.getId())
                .paymentDate(p.getPaymentDate() != null ? p.getPaymentDate().atStartOfDay() : null)
                .amount(p.getAmount().doubleValue())
                .paymentMethod(p.getMode() != null ? p.getMode().toString() : "Unknown")
                .referenceNo(p.getReceiptNo())
                .build();
    }

    private CustomerHistoryResponse.MessageSummary mapToMessageSummary(MessageSent m) {
        return CustomerHistoryResponse.MessageSummary.builder()
                .id(m.getId())
                .sentAt(m.getSentAt())
                .templateName(m.getTemplate() != null ? m.getTemplate().getName() : "Direct Message")
                .status("SENT") // MessageSent entity doesn't have status, defaulting to SENT
                .build();
    }
}
