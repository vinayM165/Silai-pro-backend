package com.silaipro.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.silaipro.dto.auth.LoginRequest;
import com.silaipro.dto.auth.SignupRequest;
import com.silaipro.dto.auth.TokenRefreshRequest;
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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@DisplayName("Auth API Integration Tests")
public class AuthControllerTest {

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

    private Role adminRole;
    private Role staffRole;

    @BeforeEach
    void setup() {
        userRepository.deleteAll();
        roleRepository.deleteAll();

        // Seed Admin role
        adminRole = new Role();
        adminRole.setName("Admin");
        adminRole.setPermissionsJson("[\"ALL_PERMISSIONS\"]");
        roleRepository.save(adminRole);

        // Seed Staff role
        staffRole = new Role();
        staffRole.setName("Staff");
        staffRole.setPermissionsJson("[\"CUSTOMER_VIEW\",\"ORDER_VIEW\"]");
        roleRepository.save(staffRole);

        // Seed a test user for login tests
        User user = new User();
        user.setName("Test Admin");
        user.setPhone("9876543210");
        user.setEmail("admin@test.com");
        user.setPasswordHash(passwordEncoder.encode("admin123"));
        user.setIsActive(true);
        user.setRole(adminRole);
        userRepository.save(user);

        // Seed a DISABLED user to test 403
        User disabledUser = new User();
        disabledUser.setName("Disabled User");
        disabledUser.setPhone("1111111111");
        disabledUser.setEmail("disabled@test.com");
        disabledUser.setPasswordHash(passwordEncoder.encode("pass123"));
        disabledUser.setIsActive(false);
        disabledUser.setRole(staffRole);
        userRepository.save(disabledUser);
    }

    // ─────────────── SIGNUP TESTS ───────────────

    @Test
    @DisplayName("Signup: SUCCESS with Admin role")
    void testSignupSuccess_WithAdminRole() throws Exception {
        SignupRequest req = new SignupRequest();
        req.setName("New Admin");
        req.setPhone("9000000001");
        req.setEmail("newadmin@test.com");
        req.setPassword("newpass123");
        req.setRoleName("Admin");

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.user.name").value("New Admin"))
                .andExpect(jsonPath("$.user.role").value("Admin"));
    }

    @Test
    @DisplayName("Signup: SUCCESS with Staff role assigned")
    void testSignupSuccess_WithStaffRole() throws Exception {
        SignupRequest req = new SignupRequest();
        req.setName("New Staff");
        req.setPhone("9000000002");
        req.setPassword("staffpass123");
        req.setRoleName("Staff");

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.user.role").value("Staff"));
    }

    @Test
    @DisplayName("Signup: SUCCESS defaults to Admin when roleName is omitted")
    void testSignupSuccess_DefaultRole() throws Exception {
        SignupRequest req = new SignupRequest();
        req.setName("Default Role User");
        req.setPhone("9000000003");
        req.setPassword("mypassword");
        // roleName NOT set → should default to "Admin"

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.user.role").value("Admin"));
    }

    @Test
    @DisplayName("Signup: ERROR 409 when phone already registered")
    void testSignupFails_DuplicatePhone() throws Exception {
        SignupRequest req = new SignupRequest();
        req.setName("Duplicate User");
        req.setPhone("9876543210");  // already seeded in @BeforeEach
        req.setPassword("somepass");

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Signup: ERROR 409 when email already registered")
    void testSignupFails_DuplicateEmail() throws Exception {
        SignupRequest req = new SignupRequest();
        req.setName("Email Dup");
        req.setPhone("9000000099");
        req.setEmail("admin@test.com");  // already seeded
        req.setPassword("somepass");

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Signup: ERROR 400 when invalid role provided")
    void testSignupFails_InvalidRole() throws Exception {
        SignupRequest req = new SignupRequest();
        req.setName("Bad Role User");
        req.setPhone("9000000004");
        req.setPassword("goodpass123");
        req.setRoleName("SuperHacker"); // invalid

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Signup: ERROR 400 when required fields missing")
    void testSignupFails_ValidationErrors() throws Exception {
        SignupRequest req = new SignupRequest();
        // name, phone, password all missing

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    // ─────────────── LOGIN TESTS ───────────────

    @Test
    @DisplayName("Login: SUCCESS with phone number")
    void testLogin_WithPhone() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setLogin("9876543210");
        req.setPassword("admin123");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.user.name").value("Test Admin"))
                .andExpect(jsonPath("$.user.role").value("Admin"));
    }

    @Test
    @DisplayName("Login: SUCCESS with email address")
    void testLogin_WithEmail() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setLogin("admin@test.com");
        req.setPassword("admin123");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.user.name").value("Test Admin"));
    }

    @Test
    @DisplayName("Login: ERROR 401 with wrong password")
    void testLogin_WrongPassword() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setLogin("9876543210");
        req.setPassword("wrongpass");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Login: ERROR 401 with unknown phone/email")
    void testLogin_UnknownUser() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setLogin("0000000000");
        req.setPassword("somepass");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Login: ERROR 403 when account is deactivated")
    void testLogin_DisabledAccount() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setLogin("1111111111");
        req.setPassword("pass123");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    // ─────────────── REFRESH TOKEN TESTS ───────────────

    @Test
    @DisplayName("Refresh: SUCCESS returns new accessToken")
    void testRefreshToken_Success() throws Exception {
        // Step 1: Login to get refreshToken
        LoginRequest loginReq = new LoginRequest();
        loginReq.setLogin("9876543210");
        loginReq.setPassword("admin123");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andReturn();

        String refreshToken = objectMapper.readTree(
                loginResult.getResponse().getContentAsString()
        ).get("refreshToken").asText();

        // Step 2: Refresh
        TokenRefreshRequest refreshReq = new TokenRefreshRequest();
        refreshReq.setRefreshToken(refreshToken);

        mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists());
    }

    @Test
    @DisplayName("Refresh: ERROR 401 with fake/invalid refreshToken")
    void testRefreshToken_Invalid() throws Exception {
        TokenRefreshRequest req = new TokenRefreshRequest();
        req.setRefreshToken("this.is.a.fake.token");

        mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    // ─────────────── LOGOUT TESTS ───────────────

    @Test
    @DisplayName("Logout: SUCCESS with valid Bearer token")
    void testLogout_WithValidToken() throws Exception {
        // Login first to get token
        LoginRequest loginReq = new LoginRequest();
        loginReq.setLogin("9876543210");
        loginReq.setPassword("admin123");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andReturn();

        String accessToken = objectMapper.readTree(
                loginResult.getResponse().getContentAsString()
        ).get("accessToken").asText();

        // Logout with Bearer token
        mockMvc.perform(post("/api/auth/logout")
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Logged out")));
    }

    @Test
    @DisplayName("Logout: ERROR 403 with no token (Spring Security blocks unauthenticated requests)")
    void testLogout_WithoutToken() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isForbidden()); // Spring Security 6 returns 403 for missing token
    }
}
