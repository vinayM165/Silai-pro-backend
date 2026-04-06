package com.silaipro.dto.user;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PasswordResetRequest {
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String newPassword;

    @Size(min = 4, max = 6, message = "PIN must be between 4 and 6 digits")
    private String newPin;
}
