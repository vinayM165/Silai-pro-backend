package com.silaipro.controller;

import org.springframework.test.annotation.DirtiesContext;



import com.fasterxml.jackson.databind.ObjectMapper;
import com.silaipro.dto.auth.LoginRequest;
import com.silaipro.dto.customer.CustomerRequest;
import com.silaipro.dto.order.OrderItem;
import com.silaipro.dto.order.OrderRequest;
import com.silaipro.entity.Role;
import com.silaipro.entity.User;
import com.silaipro.enums.OrderPriority;
import com.silaipro.enums.OrderStatus;
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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Order API Integration Tests")


@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

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
    private Long customerId;

    @BeforeEach
    void setup() throws Exception {
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

        // Seed a customer
        CustomerRequest custReq = CustomerRequest.builder()
                .name("Jane Customer")
                .phone("1112223334")
                .build();
        MvcResult custResult = mockMvc.perform(post("/api/customers")
                .header("Authorization", adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(custReq)))
                .andReturn();
        customerId = objectMapper.readTree(custResult.getResponse().getContentAsString()).get("id").asLong();
    }

    @Test
    @DisplayName("Create Order: Success")
    void testCreateOrder_Success() throws Exception {
        OrderRequest req = new OrderRequest();
        req.setCustomerId(customerId);
        req.setExpectedDelivery(LocalDate.now().plusWeeks(1));
        req.setPriority(OrderPriority.NORMAL);

        OrderItem item = OrderItem.builder()
                .description("Shirt")
                .quantity(1)
                .pricePerPiece(new BigDecimal("500"))
                .amount(new BigDecimal("500"))
                .build();
        req.setItems(List.of(item));

        mockMvc.perform(post("/api/orders")
                .header("Authorization", adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderNo").exists())
                .andExpect(jsonPath("$.totalAmount").value(500))
                .andExpect(jsonPath("$.status").value("RECEIVED"));
    }

    @Test
    @DisplayName("Status Transition: Valid Flow")
    void testStatusTransition_Valid() throws Exception {
        // Create an order first
        OrderRequest req = new OrderRequest();
        req.setCustomerId(customerId);
        req.setExpectedDelivery(LocalDate.now().plusWeeks(1));
        req.setItems(List.of(OrderItem.builder().description("Pant").quantity(1).pricePerPiece(new BigDecimal("600")).amount(new BigDecimal("600")).build()));

        MvcResult createResult = mockMvc.perform(post("/api/orders")
                .header("Authorization", adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andReturn();
        Long orderId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        // Transition: RECEIVED -> STITCHING
        mockMvc.perform(patch("/api/orders/" + orderId + "/status")
                .header("Authorization", adminToken)
                .param("status", "STITCHING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("STITCHING"));

        // Transition: STITCHING -> DELIVERED
        mockMvc.perform(patch("/api/orders/" + orderId + "/status")
                .header("Authorization", adminToken)
                .param("status", "DELIVERED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DELIVERED"));
    }

    @Test
    @DisplayName("Dashboard: Get Order Counts")
    void testGetOrderCounts() throws Exception {
        mockMvc.perform(get("/api/orders/dashboard-counts")
                .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.RECEIVED").exists());
    }
}
