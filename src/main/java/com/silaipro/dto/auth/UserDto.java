package com.silaipro.dto.auth;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDto {
    private Long id;
    private String name;
    private String phone;
    private String role;
}
