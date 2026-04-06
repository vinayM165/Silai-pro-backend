package com.silaipro.service;

import com.silaipro.dto.billing.InvoiceResponse;
import com.silaipro.dto.reports.*;
import com.silaipro.entity.*;
import com.silaipro.enums.PaymentMode;
import com.silaipro.enums.PaymentStatus;
import com.silaipro.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportsService {

    private final InvoiceRepository invoiceRepository;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final InvoiceService invoiceService;

    public DashboardSummaryResponse getDashboardSummary(LocalDate from, LocalDate to) {
        BigDecimal totalRevenue = invoiceRepository.sumRevenueBetween(from, to);
        BigDecimal amountReceived = paymentRepository.sumPaymentsBetween(from, to);
        
        // Sum of balanceDue for invoices created in this range
        // Note: As per assumption in implementation plan, we count dues for invoices in range.
        BigDecimal pendingDues = totalRevenue.subtract(amountReceived);

        long ordersCreated = orderRepository.countCreatedBetween(from.atStartOfDay(), to.atTime(23, 59, 59));
        long ordersDelivered = orderRepository.countDeliveredBetween(from.atStartOfDay(), to.atTime(23, 59, 59));

        return DashboardSummaryResponse.builder()
                .totalRevenue(totalRevenue)
                .amountReceived(amountReceived)
                .pendingDues(pendingDues)
                .ordersCreated(ordersCreated)
                .ordersDelivered(ordersDelivered)
                .build();
    }

    public List<DailyCollectionRow> getDailyCollectionReport(LocalDate from, LocalDate to) {
        List<Payment> payments = paymentRepository.findByPaymentDateBetweenOrderByPaymentDateAsc(from, to);
        
        Map<LocalDate, List<Payment>> grouped = payments.stream()
                .collect(Collectors.groupingBy(Payment::getPaymentDate));

        return grouped.entrySet().stream()
                .map(entry -> {
                    LocalDate date = entry.getKey();
                    List<Payment> dailyPayments = entry.getValue();
                    
                    BigDecimal cash = dailyPayments.stream()
                            .filter(p -> p.getMode() == PaymentMode.CASH)
                            .map(Payment::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    
                    BigDecimal upi = dailyPayments.stream()
                            .filter(p -> p.getMode() == PaymentMode.UPI)
                            .map(Payment::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    
                    BigDecimal total = dailyPayments.stream()
                            .map(Payment::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    return DailyCollectionRow.builder()
                            .date(date)
                            .cashCollected(cash)
                            .upiCollected(upi)
                            .totalCollected(total)
                            .paymentCount(dailyPayments.size())
                            .build();
                })
                .sorted(Comparator.comparing(DailyCollectionRow::getDate))
                .collect(Collectors.toList());
    }

    public List<PendingDueRow> getPendingDuesReport() {
        List<Invoice> pendingInvoices = invoiceRepository.findAllWithPendingBalance();
        
        return pendingInvoices.stream().map(invoice -> {
            BigDecimal totalPaid = invoice.getPayments().stream()
                    .map(Payment::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            LocalDate lastPaymentDate = invoice.getPayments().stream()
                    .map(Payment::getPaymentDate)
                    .max(LocalDate::compareTo)
                    .orElse(null);

            return PendingDueRow.builder()
                    .customerId(invoice.getOrder().getCustomer().getId())
                    .customerName(invoice.getOrder().getCustomer().getName())
                    .phone(invoice.getOrder().getCustomer().getPhone())
                    .invoiceId(invoice.getId())
                    .invoiceNo(invoice.getInvoiceNo())
                    .invoiceTotal(invoice.getTotal())
                    .totalPaid(totalPaid)
                    .balanceDue(invoice.getTotal().subtract(totalPaid))
                    .daysPending(ChronoUnit.DAYS.between(invoice.getInvoiceDate(), LocalDate.now()))
                    .lastPaymentDate(lastPaymentDate)
                    .build();
        }).collect(Collectors.toList());
    }

    public CustomerLedgerResponse getCustomerLedger(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));
        
        List<Invoice> invoices = invoiceRepository.findByOrderCustomerId(customerId);
        
        return CustomerLedgerResponse.builder()
                .customer(mapToCustomerResponse(customer))
                .invoices(invoices.stream().map(invoiceService::mapToResponse).collect(Collectors.toList()))
                .build();
    }

    public List<StaffPerformanceRow> getStaffPerformance(LocalDate from, LocalDate to) {
        List<User> staffMembers = userRepository.findAll(); // Simple approach, can be filtered by role
        
        return staffMembers.stream().map(staff -> {
            long ordersHandled = orderRepository.findByAssignedToIdAndCreatedAtBetween(
                    staff.getId(), from.atStartOfDay(), to.atTime(23, 59, 59)).size();
            
            List<Payment> collections = paymentRepository.findByReceivedByIdAndDateRange(
                    staff.getId(), from, to);
            
            BigDecimal totalCollected = collections.stream()
                    .map(Payment::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            return StaffPerformanceRow.builder()
                    .staffId(staff.getId())
                    .name(staff.getName())
                    .ordersHandled(ordersHandled)
                    .collectionsRecorded((long) collections.size())
                    .totalCollected(totalCollected)
                    .build();
        }).filter(row -> row.getOrdersHandled() > 0 || row.getCollectionsRecorded() > 0)
        .collect(Collectors.toList());
    }

    public List<InvoiceResponse> getOverdueInvoices() {
        List<Invoice> overdue = invoiceRepository.findOverdueInvoices(LocalDate.now());
        return overdue.stream().map(invoiceService::mapToResponse).collect(Collectors.toList());
    }

    public List<MonthlyRevenueRow> getMonthlyRevenueSummary(int year) {
        List<Object[]> revenueData = invoiceRepository.revenueByMonth(year);
        List<Object[]> collectionData = paymentRepository.collectionsByMonth(year);

        Map<Integer, BigDecimal> revenueMap = revenueData.stream()
                .collect(Collectors.toMap(row -> (Integer) row[0], row -> (BigDecimal) row[1]));
        
        Map<Integer, BigDecimal> collectionMap = collectionData.stream()
                .collect(Collectors.toMap(row -> (Integer) row[0], row -> (BigDecimal) row[1]));

        List<MonthlyRevenueRow> summary = new ArrayList<>();
        for (int m = 1; m <= 12; m++) {
            BigDecimal revenue = revenueMap.getOrDefault(m, BigDecimal.ZERO);
            BigDecimal collections = collectionMap.getOrDefault(m, BigDecimal.ZERO);
            
            summary.add(MonthlyRevenueRow.builder()
                    .month(m)
                    .monthName(LocalDate.of(year, m, 1).getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH))
                    .revenue(revenue)
                    .collections(collections)
                    .pendingDues(revenue.subtract(collections))
                    .build());
        }
        return summary;
    }

    private com.silaipro.dto.customer.CustomerResponse mapToCustomerResponse(Customer c) {
        return com.silaipro.dto.customer.CustomerResponse.builder()
                .id(c.getId())
                .name(c.getName())
                .phone(c.getPhone())
                .whatsapp(c.getWhatsapp())
                .email(c.getEmail())
                .address(c.getAddress())
                .city(c.getCity())
                .customerType(c.getCustomerType())
                .dateJoined(c.getDateJoined())
                .isActive(c.getIsActive())
                .build();
    }
}
