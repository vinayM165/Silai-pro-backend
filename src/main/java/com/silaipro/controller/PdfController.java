package com.silaipro.controller;

import com.silaipro.dto.customer.CustomerFilter;
import com.silaipro.security.HasPermission;
import com.silaipro.service.PdfService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pdf")
@RequiredArgsConstructor
@Tag(name = "PDF", description = "PDF report generation endpoints")
public class PdfController {

    private final PdfService pdfService;

    @GetMapping("/measurement/{id}")
    @HasPermission("MEASUREMENT_VIEW")
    @Operation(summary = "Generate PDF for a measurement record")
    public ResponseEntity<byte[]> getMeasurementPdf(@PathVariable Long id) {
        byte[] pdf = pdfService.generateMeasurementPdf(id);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"measurement-" + id + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/invoice/{id}")
    @HasPermission("ORDER_VIEW")
    @Operation(summary = "Generate PDF for an invoice")
    public ResponseEntity<byte[]> getInvoicePdf(@PathVariable Long id) {
        byte[] pdf = pdfService.generateInvoicePdf(id);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"invoice-" + id + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/customers")
    @HasPermission("ADMIN")
    @Operation(summary = "Generate PDF for customer list (Admin Only)")
    public ResponseEntity<byte[]> getCustomerListPdf(CustomerFilter filter) {
        byte[] pdf = pdfService.generateCustomerListPdf(filter);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"customer-list.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
