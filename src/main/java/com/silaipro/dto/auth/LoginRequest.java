package com.silaipro.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank(message = "Phone or email login identifier is required")
    private String login; // Handles both phone or email

    private String password;
    private String pin;
}
