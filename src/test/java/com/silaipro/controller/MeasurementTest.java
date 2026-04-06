package com.silaipro.controller;

import org.springframework.test.annotation.DirtiesContext;



import com.fasterxml.jackson.databind.ObjectMapper;
import com.silaipro.dto.auth.LoginRequest;
import com.silaipro.dto.customer.CustomerRequest;
import com.silaipro.dto.measurement.CategoryRequest;
import com.silaipro.dto.measurement.FieldRequest;
import com.silaipro.dto.measurement.MeasurementRequest;
import com.silaipro.entity.Role;
import com.silaipro.entity.User;
import com.silaipro.enums.FieldType;
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

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.hasSize;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")

@DisplayName("Customer Measurement Integration Tests")


@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class MeasurementTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MeasurementCategoryRepository categoryRepository;

    @Autowired
    private MeasurementFieldRepository fieldRepository;

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
    private Long categoryId;
    private Long fieldId;

    @BeforeEach
    void setup() throws Exception {
        fieldRepository.deleteAll();
        categoryRepository.deleteAll();
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

        // Seed Customer
        CustomerRequest custReq = CustomerRequest.builder().name("John Measurement").phone("1212121212").build();
        MvcResult custResult = mockMvc.perform(post("/api/customers").header("Authorization", adminToken).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(custReq))).andReturn();
        customerId = objectMapper.readTree(custResult.getResponse().getContentAsString()).get("id").asLong();

        // Seed Category
        CategoryRequest catReq = new CategoryRequest();
        catReq.setName("Shirt");
        MvcResult catResult = mockMvc.perform(post("/api/measurement-config/categories").header("Authorization", adminToken).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(catReq))).andReturn();
        categoryId = objectMapper.readTree(catResult.getResponse().getContentAsString()).get("id").asLong();

        // Seed Field
        FieldRequest fieldReq = new FieldRequest();
        fieldReq.setFieldName("Chest");
        fieldReq.setFieldType(FieldType.NUMBER);
        fieldReq.setUnit("inches");
        MvcResult fieldResult = mockMvc.perform(post("/api/measurement-config/categories/" + categoryId + "/fields").header("Authorization", adminToken).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(fieldReq))).andReturn();
        fieldId = objectMapper.readTree(fieldResult.getResponse().getContentAsString()).get("id").asLong();
    }

    @Test
    @DisplayName("Measurement: Save and Retrieve")
    void testSaveAndRetrieveMeasurement() throws Exception {
        MeasurementRequest req = new MeasurementRequest();
        req.setCustomerId(customerId);
        req.setCategoryId(categoryId);
        
        MeasurementRequest.MeasurementValueRequest val = new MeasurementRequest.MeasurementValueRequest();
        val.setFieldId(fieldId);
        val.setValue("42");
        req.setValues(List.of(val));

        mockMvc.perform(post("/api/measurements")
                .header("Authorization", adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.values", hasSize(1)))
                .andExpect(jsonPath("$.values[0].value").value("42"));

        mockMvc.perform(get("/api/measurements/customer/" + customerId)
                .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].categoryName").value("Shirt"));
    }
}
