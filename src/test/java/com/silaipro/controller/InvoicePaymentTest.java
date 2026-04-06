package com.silaipro.controller;

import org.springframework.test.annotation.DirtiesContext;



import com.fasterxml.jackson.databind.ObjectMapper;
import com.silaipro.dto.auth.LoginRequest;
import com.silaipro.dto.billing.InvoiceRequest;
import com.silaipro.dto.billing.PaymentRequest;
import com.silaipro.dto.customer.CustomerRequest;
import com.silaipro.dto.order.OrderItem;
import com.silaipro.dto.order.OrderRequest;
import com.silaipro.entity.Role;
import com.silaipro.entity.User;
import com.silaipro.enums.PaymentMode;
import com.silaipro.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Invoice & Payment Integration Tests")


@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class InvoicePaymentTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken;
    private Long orderId;

    @BeforeEach
    void setup() throws Exception {
        paymentRepository.deleteAll();
        invoiceRepository.deleteAll();
        orderRepository.deleteAll();
        customerRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();

        // Seed Admin role
        Role adminRole = new Role();
        adminRole.setName("Admin");
        adminRole.setPermissionsJson("[\"ALL_PERMISSIONS\"]");
        roleRepository.save(adminRole);

        // Seed Admin user
        User user = new User();
        user.setName("Test Admin");
        user.setPhone("9876543210");
        user.setPasswordHash(passwordEncoder.encode("admin123"));
        user.setIsActive(true);
        user.setRole(adminRole);
        userRepository.save(user);

        // Login to get token
        LoginRequest loginReq = new LoginRequest();
        loginReq.setLogin("9876543210");
        loginReq.setPassword("admin123");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginReq)))
                .andReturn();

        adminToken = "Bearer " + objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .get("accessToken").asText();

        // Seed a customer and an order
        CustomerRequest custReq = CustomerRequest.builder().name("John Billing").phone("1234567890").build();
        MvcResult custResult = mockMvc.perform(post("/api/customers").header("Authorization", adminToken).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(custReq))).andReturn();
        Long custId = objectMapper.readTree(custResult.getResponse().getContentAsString()).get("id").asLong();

        OrderRequest orderReq = new OrderRequest();
        orderReq.setCustomerId(custId);
        orderReq.setExpectedDelivery(LocalDate.now().plusDays(5));
        orderReq.setItems(List.of(OrderItem.builder().description("Suit").quantity(1).pricePerPiece(new BigDecimal("2000")).amount(new BigDecimal("2000")).build()));
        
        MvcResult orderResult = mockMvc.perform(post("/api/orders").header("Authorization", adminToken).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(orderReq))).andReturn();
        orderId = objectMapper.readTree(orderResult.getResponse().getContentAsString()).get("id").asLong();
    }

    @Test
    @DisplayName("Billing: Generate Invoice and Record Payments")
    void testInvoicePaymentFlow() throws Exception {
        // 1. Generate Invoice
        InvoiceRequest invReq = new InvoiceRequest();
        invReq.setOrderId(orderId);
        invReq.setDueDate(LocalDate.now().plusDays(2));

        MvcResult invResult = mockMvc.perform(post("/api/billing/invoices")
                .header("Authorization", adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.invoiceNo").exists())
                .andExpect(jsonPath("$.paymentStatus").value("UNPAID"))
                .andExpect(jsonPath("$.balanceDue").value(2000))
                .andReturn();

        Long invoiceId = objectMapper.readTree(invResult.getResponse().getContentAsString()).get("id").asLong();

        // 2. Record Partial Payment
        PaymentRequest payReq = new PaymentRequest();
        payReq.setInvoiceId(invoiceId);
        payReq.setAmount(new BigDecimal("500"));
        payReq.setMode(PaymentMode.CASH);
        payReq.setPaymentDate(LocalDate.now());

        mockMvc.perform(post("/api/billing/payments")
                .header("Authorization", adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payReq)))
                .andExpect(status().isCreated());

        // Check Invoice status
        mockMvc.perform(get("/api/billing/invoices/" + invoiceId).header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentStatus").value("PARTIAL"))
                .andExpect(jsonPath("$.totalPaid").value(500))
                .andExpect(jsonPath("$.balanceDue").value(1500));

        // 3. Record Full Payment
        payReq.setAmount(new BigDecimal("1500"));
        mockMvc.perform(post("/api/billing/payments")
                .header("Authorization", adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payReq)))
                .andExpect(status().isCreated());

        // Check Final Invoice status
        mockMvc.perform(get("/api/billing/invoices/" + invoiceId).header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentStatus").value("PAID"))
                .andExpect(jsonPath("$.balanceDue").value(0))
                .andExpect(jsonPath("$.totalPaid").value(2000));
    }
}
