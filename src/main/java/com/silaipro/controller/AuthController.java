package com.silaipro.controller;

import com.silaipro.dto.auth.*;
import com.silaipro.entity.User;
import com.silaipro.repository.RoleRepository;
import com.silaipro.repository.UserRepository;
import com.silaipro.security.CustomUserDetails;
import com.silaipro.security.JwtUtil;
import com.silaipro.security.UserDetailsServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Authentication and registration endpoints")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/signup")
    @Operation(summary = "Register a new user (admin by default)")
    public ResponseEntity<AuthResponse> signup(@Valid @RequestBody SignupRequest request) {
        if (userRepository.findByPhone(request.getPhone()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Phone already registered");
        }

        String roleName = request.getRoleName() != null ? request.getRoleName() : "Admin";
        var role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid role name"));

        User user = User.builder()
                .name(request.getName())
                .phone(request.getPhone())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .isActive(true)
                .build();

        user = userRepository.save(user);

        UserDetails userDetails = new CustomUserDetails(user);
        String accessToken = jwtUtil.generateAccessToken(userDetails);
        String refreshToken = jwtUtil.generateRefreshToken(userDetails);

        return ResponseEntity.status(HttpStatus.CREATED).body(AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(mapToDto(user))
                .build());
    }

    @PostMapping("/login")
    @Operation(summary = "Login to get access and refresh tokens")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        User user = userRepository.findByPhoneOrEmail(request.getLogin())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!user.getIsActive()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account deactivated");
        }

        if (request.getPassword() != null) {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getLogin(), request.getPassword())
            );
        } else if (request.getPin() != null) {
            if (user.getPinHash() == null || !passwordEncoder.matches(request.getPin(), user.getPinHash())) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid PIN");
            }
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password or PIN required");
        }

        UserDetails userDetails = new CustomUserDetails(user);
        String accessToken = jwtUtil.generateAccessToken(userDetails);
        String refreshToken = jwtUtil.generateRefreshToken(userDetails);

        return ResponseEntity.ok(AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(mapToDto(user))
                .build());
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token using a refresh token")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody TokenRefreshRequest request) {
        String refreshToken = request.getRefreshToken();
        String username = jwtUtil.extractUsername(refreshToken);

        if (username != null && jwtUtil.validateToken(refreshToken, userDetailsService.loadUserByUsername(username))) {
            User user = userRepository.findByPhoneOrEmail(username)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
            
            UserDetails userDetails = new CustomUserDetails(user);
            String newAccessToken = jwtUtil.generateAccessToken(userDetails);

            return ResponseEntity.ok(AuthResponse.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(refreshToken)
                    .user(mapToDto(user))
                    .build());
        }

        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout user")
    public ResponseEntity<String> logout() {
        return ResponseEntity.ok("Logged out successfully");
    }

    private UserDto mapToDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .phone(user.getPhone())
                .role(user.getRole().getName())
                .build();
    }
}
