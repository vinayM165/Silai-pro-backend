package com.silaipro.controller;

import org.springframework.test.annotation.DirtiesContext;



import com.fasterxml.jackson.databind.ObjectMapper;
import com.silaipro.dto.auth.LoginRequest;
import com.silaipro.entity.Role;
import com.silaipro.entity.User;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Role-Based Access Control Integration Tests")


@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class RBACTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken;
    private String staffToken;

    @BeforeEach
    void setup() throws Exception {
        userRepository.deleteAll();
        roleRepository.deleteAll();

        // Admin Role & User
        Role adminRole = new Role();
        adminRole.setName("Admin");
        adminRole.setPermissionsJson("[\"ADMIN\"]");
        roleRepository.save(adminRole);

        User adminUser = new User();
        adminUser.setName("Admin User");
        adminUser.setPhone("1111111111");
        adminUser.setPasswordHash(passwordEncoder.encode("admin123"));
        adminUser.setRole(adminRole);
        userRepository.save(adminUser);

        // Staff Role & User (No ADMIN permission)
        Role staffRole = new Role();
        staffRole.setName("Staff");
        staffRole.setPermissionsJson("[\"VIEW_ORDERS\"]");
        roleRepository.save(staffRole);

        User staffUser = new User();
        staffUser.setName("Staff User");
        staffUser.setPhone("2222222222");
        staffUser.setPasswordHash(passwordEncoder.encode("staff123"));
        staffUser.setRole(staffRole);
        userRepository.save(staffUser);

        // Get Tokens
        adminToken = "Bearer " + getToken("1111111111", "admin123");
        staffToken = "Bearer " + getToken("2222222222", "staff123");
    }

    private String getToken(String login, String password) throws Exception {
        LoginRequest req = new LoginRequest();
        req.setLogin(login);
        req.setPassword(password);

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString()).get("accessToken").asText();
    }

    @Test
    @DisplayName("RBAC: Admin can access settings")
    void testAdminAccess_Settings() throws Exception {
        mockMvc.perform(get("/api/settings")
                .header("Authorization", adminToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("RBAC: Staff cannot access settings (403)")
    void testStaffAccess_Settings_Forbidden() throws Exception {
        mockMvc.perform(get("/api/settings")
                .header("Authorization", staffToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("RBAC: Unauthenticated access returns 401")
    void testUnauthenticatedAccess() throws Exception {
        mockMvc.perform(get("/api/settings"))
                .andExpect(status().isUnauthorized());
    }
}
