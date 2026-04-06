package com.silaipro.controller;

import com.silaipro.dto.billing.InvoiceResponse;
import com.silaipro.dto.reports.*;
import com.silaipro.security.HasPermission;
import com.silaipro.service.ReportsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Reports", description = "Business intelligence and reports")
public class ReportsController {

    private final ReportsService reportsService;

    @GetMapping("/api/reports/dashboard")
    @HasPermission("ACCOUNTS_VIEW")
    @Operation(summary = "Get dashboard summary data")
    public DashboardSummaryResponse getDashboardSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return reportsService.getDashboardSummary(from, to);
    }

    @GetMapping("/api/reports/daily-collection")
    @HasPermission("ACCOUNTS_REPORT")
    @Operation(summary = "Get daily collection report")
    public List<DailyCollectionRow> getDailyCollectionReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return reportsService.getDailyCollectionReport(from, to);
    }

    @GetMapping("/api/reports/pending-dues")
    @HasPermission("ACCOUNTS_REPORT")
    @Operation(summary = "Get pending dues report")
    public List<PendingDueRow> getPendingDuesReport() {
        return reportsService.getPendingDuesReport();
    }

    @GetMapping("/api/customers/{id}/ledger")
    @HasPermission("ACCOUNTS_VIEW")
    @Operation(summary = "Get customer ledger (orders and payments)")
    public CustomerLedgerResponse getCustomerLedger(@PathVariable Long id) {
        return reportsService.getCustomerLedger(id);
    }

    @GetMapping("/api/reports/staff-performance")
    @HasPermission("ACCOUNTS_REPORT")
    @Operation(summary = "Get staff performance report")
    public List<StaffPerformanceRow> getStaffPerformance(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return reportsService.getStaffPerformance(from, to);
    }

    @GetMapping("/api/reports/overdue-invoices")
    @HasPermission("ACCOUNTS_VIEW")
    @Operation(summary = "Get list of overdue invoices")
    public List<InvoiceResponse> getOverdueInvoices() {
        return reportsService.getOverdueInvoices();
    }

    @GetMapping("/api/reports/monthly-summary")
    @HasPermission("ACCOUNTS_REPORT")
    @Operation(summary = "Get monthly revenue summary for a year")
    public List<MonthlyRevenueRow> getMonthlyRevenueSummary(@RequestParam int year) {
        return reportsService.getMonthlyRevenueSummary(year);
    }
}
