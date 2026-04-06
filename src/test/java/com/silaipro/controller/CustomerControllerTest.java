package com.silaipro.controller;

import org.springframework.test.annotation.DirtiesContext;



import com.fasterxml.jackson.databind.ObjectMapper;
import com.silaipro.dto.auth.LoginRequest;
import com.silaipro.dto.customer.CustomerRequest;
import com.silaipro.entity.Role;
import com.silaipro.entity.User;
import com.silaipro.repository.CustomerRepository;
import com.silaipro.repository.RoleRepository;
import com.silaipro.repository.UserRepository;
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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Customer API Integration Tests")


@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

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

    @BeforeEach
    void setup() throws Exception {
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

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginReq)))
                .andReturn();

        adminToken = "Bearer " + objectMapper.readTree(result.getResponse().getContentAsString())
                .get("accessToken").asText();
    }

    @Test
    @DisplayName("CRUD: Create Customer Success")
    void testCreateCustomer_Success() throws Exception {
        CustomerRequest req = CustomerRequest.builder()
                .name("John Doe")
                .phone("1234567890")
                .email("john@example.com")
                .build();

        mockMvc.perform(post("/api/customers")
                .header("Authorization", adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.phone").value("1234567890"));
    }

    @Test
    @DisplayName("CRUD: Get Customer By ID")
    void testGetCustomerById() throws Exception {
        CustomerRequest req = CustomerRequest.builder()
                .name("Jane Smith")
                .phone("0987654321")
                .build();

        MvcResult createResult = mockMvc.perform(post("/api/customers")
                .header("Authorization", adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andReturn();

        Long id = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(get("/api/customers/" + id)
                .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Jane Smith"));
    }

    @Test
    @DisplayName("CRUD: Update Customer")
    void testUpdateCustomer() throws Exception {
        CustomerRequest req = CustomerRequest.builder()
                .name("Old Name")
                .phone("1112223334")
                .build();

        MvcResult createResult = mockMvc.perform(post("/api/customers")
                .header("Authorization", adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andReturn();

        Long id = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        req.setName("Updated Name");
        mockMvc.perform(put("/api/customers/" + id)
                .header("Authorization", adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"));
    }

    @Test
    @DisplayName("Search & Pagination: Find Customer")
    void testSearchAndPagination() throws Exception {
        // Seed some customers
        seedCustomer("Alice", "1111111111");
        seedCustomer("Bob", "2222222222");
        seedCustomer("Charlie", "3333333333");

        mockMvc.perform(get("/api/customers")
                .header("Authorization", adminToken)
                .param("search", "Alice")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name").value("Alice"));
        
        mockMvc.perform(get("/api/customers")
                .header("Authorization", adminToken)
                .param("page", "0")
                .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)));
    }

    private void seedCustomer(String name, String phone) throws Exception {
        CustomerRequest req = CustomerRequest.builder().name(name).phone(phone).build();
        mockMvc.perform(post("/api/customers")
                .header("Authorization", adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)));
    }
}
