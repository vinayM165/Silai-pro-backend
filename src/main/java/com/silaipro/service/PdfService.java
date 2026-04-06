package com.silaipro.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.silaipro.constant.ShopSettingConstants;
import com.silaipro.dto.customer.CustomerFilter;
import com.silaipro.entity.*;
import com.silaipro.repository.CustomerRepository;
import com.silaipro.repository.InvoiceRepository;
import com.silaipro.repository.MeasurementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PdfService {

    private final MeasurementRepository measurementRepository;
    private final InvoiceRepository invoiceRepository;
    private final CustomerRepository customerRepository;
    private final ShopSettingService shopSettingService;
    private final ObjectMapper objectMapper;

    public byte[] generateMeasurementPdf(Long measurementId) {
        Measurement m = measurementRepository.findById(measurementId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Measurement not found"));

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            addShopHeader(document);

            document.add(new Paragraph("MEASUREMENT RECORD").setBold().setFontSize(16).setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph("Garment: " + m.getCategory().getName()).setBold().setFontSize(14));

            // Customer Info
            document.add(new Paragraph("Customer: " + m.getCustomer().getName() + " (ID: " + m.getCustomer().getId() + ")"));
            document.add(new Paragraph("Phone: " + m.getCustomer().getPhone()));
            document.add(new Paragraph("\n"));

            // Values Table
            Table table = new Table(UnitValue.createPointArray(new float[]{250f, 250f}));
            table.addHeaderCell(new Cell().add(new Paragraph("Field")).setBackgroundColor(new DeviceRgb(240, 240, 240)));
            table.addHeaderCell(new Cell().add(new Paragraph("Value")).setBackgroundColor(new DeviceRgb(240, 240, 240)));

            for (MeasurementValue val : m.getValues()) {
                table.addCell(new Cell().add(new Paragraph(val.getField().getFieldName())));
                table.addCell(new Cell().add(new Paragraph(val.getFieldValue())));
            }
            document.add(table);

            if (m.getNotes() != null && !m.getNotes().isEmpty()) {
                document.add(new Paragraph("\nNotes: " + m.getNotes()));
            }

            document.add(new Paragraph("\nTaken by: " + (m.getTakenBy() != null ? m.getTakenBy().getName() : "System")));
            document.add(new Paragraph("Date: " + m.getTakenAt().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm"))));

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "PDF Generation Error: " + e.getMessage());
        }
    }

    public byte[] generateInvoicePdf(Long invoiceId) {
        Invoice inv = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invoice not found"));

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            addShopHeader(document);

            Table headerInfo = new Table(UnitValue.createPercentArray(new float[]{50, 50})).useAllAvailableWidth();
            headerInfo.addCell(new Cell().add(new Paragraph("INVOICE # " + inv.getInvoiceNo()).setBold()).setBorder(com.itextpdf.layout.borders.Border.NO_BORDER));
            headerInfo.addCell(new Cell().add(new Paragraph("Date: " + inv.getInvoiceDate()).setTextAlignment(TextAlignment.RIGHT)).setBorder(com.itextpdf.layout.borders.Border.NO_BORDER));
            document.add(headerInfo);

            document.add(new Paragraph("\nBill To:").setBold());
            document.add(new Paragraph(inv.getOrder().getCustomer().getName()));
            document.add(new Paragraph("Phone: " + inv.getOrder().getCustomer().getPhone()));
            document.add(new Paragraph("\n"));

            // Items Table
            Table itemsTable = new Table(UnitValue.createPercentArray(new float[]{60, 20, 20})).useAllAvailableWidth();
            itemsTable.addHeaderCell(new Cell().add(new Paragraph("Description")).setBackgroundColor(new DeviceRgb(240, 240, 240)));
            itemsTable.addHeaderCell(new Cell().add(new Paragraph("Qty")).setBackgroundColor(new DeviceRgb(240, 240, 240)));
            itemsTable.addHeaderCell(new Cell().add(new Paragraph("Total")).setBackgroundColor(new DeviceRgb(240, 240, 240)));

            List<Map<String, Object>> items = parseItemsJson(inv.getOrder().getItemsJson());
            for (Map<String, Object> item : items) {
                itemsTable.addCell(new Cell().add(new Paragraph(String.valueOf(item.getOrDefault("description", "N/A")))));
                itemsTable.addCell(new Cell().add(new Paragraph(String.valueOf(item.getOrDefault("quantity", "1")))));
                itemsTable.addCell(new Cell().add(new Paragraph(String.valueOf(item.getOrDefault("amount", "0.00")))));
            }
            document.add(itemsTable);

            // Financial Summary
            document.add(new Paragraph("\n"));
            Table summaryTable = new Table(UnitValue.createPercentArray(new float[]{70, 30})).useAllAvailableWidth();
            summaryTable.addCell(new Cell().add(new Paragraph("Subtotal")).setBorder(com.itextpdf.layout.borders.Border.NO_BORDER));
            summaryTable.addCell(new Cell().add(new Paragraph(String.valueOf(inv.getSubtotal())).setTextAlignment(TextAlignment.RIGHT)).setBorder(com.itextpdf.layout.borders.Border.NO_BORDER));
            summaryTable.addCell(new Cell().add(new Paragraph("Discount")).setBorder(com.itextpdf.layout.borders.Border.NO_BORDER));
            summaryTable.addCell(new Cell().add(new Paragraph("- " + inv.getDiscount()).setTextAlignment(TextAlignment.RIGHT)).setBorder(com.itextpdf.layout.borders.Border.NO_BORDER));
            summaryTable.addCell(new Cell().add(new Paragraph("Tax (" + inv.getTaxRate() + "%)")).setBorder(com.itextpdf.layout.borders.Border.NO_BORDER));
            summaryTable.addCell(new Cell().add(new Paragraph(String.valueOf(inv.getTaxAmount())).setTextAlignment(TextAlignment.RIGHT)).setBorder(com.itextpdf.layout.borders.Border.NO_BORDER));
            summaryTable.addCell(new Cell().add(new Paragraph("GRAND TOTAL").setBold()).setBorder(com.itextpdf.layout.borders.Border.NO_BORDER));
            summaryTable.addCell(new Cell().add(new Paragraph(String.valueOf(inv.getTotal())).setBold().setTextAlignment(TextAlignment.RIGHT)).setBorder(com.itextpdf.layout.borders.Border.NO_BORDER));
            document.add(summaryTable);

            // Payments Table
            if (inv.getPayments() != null && !inv.getPayments().isEmpty()) {
                document.add(new Paragraph("\nPayment History:").setBold());
                Table ptsTable = new Table(UnitValue.createPercentArray(new float[]{40, 30, 30})).useAllAvailableWidth();
                ptsTable.addHeaderCell("Date");
                ptsTable.addHeaderCell("Mode");
                ptsTable.addHeaderCell("Amount");
                for (Payment p : inv.getPayments()) {
                    ptsTable.addCell(p.getPaymentDate().toString());
                    ptsTable.addCell(p.getMode().toString());
                    ptsTable.addCell(p.getAmount().toString());
                }
                document.add(ptsTable);
            }

            BigDecimal paid = inv.getPayments().stream().map(Payment::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal balance = inv.getTotal().subtract(paid);
            document.add(new Paragraph("\nBalance Due: " + balance).setBold().setFontSize(14).setTextAlignment(TextAlignment.RIGHT).setFontColor(new DeviceRgb(255, 0, 0)));

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "PDF Generation Error: " + e.getMessage());
        }
    }

    public byte[] generateCustomerListPdf(CustomerFilter filter) {
        // Simple list for now
        List<Customer> customers = customerRepository.findAll();

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            addShopHeader(document);
            document.add(new Paragraph("CUSTOMER DIRECTORY").setBold().setFontSize(16).setTextAlignment(TextAlignment.CENTER));

            Table table = new Table(UnitValue.createPercentArray(new float[]{10, 40, 25, 25})).useAllAvailableWidth();
            table.addHeaderCell("ID");
            table.addHeaderCell("Name");
            table.addHeaderCell("Phone");
            table.addHeaderCell("City");

            for (Customer c : customers) {
                table.addCell(String.valueOf(c.getId()));
                table.addCell(c.getName());
                table.addCell(c.getPhone());
                table.addCell(c.getCity() != null ? c.getCity() : "-");
            }
            document.add(table);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "PDF Generation Error: " + e.getMessage());
        }
    }

    private void addShopHeader(Document document) {
        String name = shopSettingService.getSetting(ShopSettingConstants.SHOP_NAME);
        String address = shopSettingService.getSetting(ShopSettingConstants.SHOP_ADDRESS);
        String phone = shopSettingService.getSetting(ShopSettingConstants.SHOP_PHONE);

        document.add(new Paragraph(name != null ? name : "Silai Pro").setBold().setFontSize(18).setTextAlignment(TextAlignment.CENTER));
        if (address != null) document.add(new Paragraph(address).setFontSize(10).setTextAlignment(TextAlignment.CENTER));
        if (phone != null) document.add(new Paragraph("Contact: " + phone).setFontSize(10).setTextAlignment(TextAlignment.CENTER));
        document.add(new Paragraph("\n"));
    }

    private List<Map<String, Object>> parseItemsJson(String json) {
        if (json == null || json.isEmpty()) return Collections.emptyList();
        try {
            return objectMapper.readValue(json, new TypeReference<List<Map<String, Object>>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
