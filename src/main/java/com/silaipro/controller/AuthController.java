package com.silaipro.controller;

import com.silaipro.dto.auth.AuthResponse;
import com.silaipro.dto.auth.LoginRequest;
import com.silaipro.dto.auth.SignupRequest;
import com.silaipro.dto.auth.TokenRefreshRequest;
import com.silaipro.dto.auth.UserDto;
import com.silaipro.entity.Role;
import com.silaipro.entity.User;
import com.silaipro.repository.RoleRepository;
import com.silaipro.repository.UserRepository;
import com.silaipro.security.JwtUtil;
import com.silaipro.security.UserDetailsServiceImpl;
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
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@Valid @RequestBody SignupRequest request) {
        if (userRepository.findByPhone(request.getPhone()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Phone number is already registered.");
        }
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already registered.");
            }
        }

        // Resolve role: use provided roleName or default to "Admin"
        String roleName = (request.getRoleName() != null && !request.getRoleName().trim().isEmpty())
                ? request.getRoleName().trim()
                : "Admin";

        Role assignedRole = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Role '" + roleName + "' not found. Valid roles: Admin, Manager, Staff, View Only"
                ));

        User newUser = new User();
        newUser.setName(request.getName());
        newUser.setPhone(request.getPhone());
        newUser.setEmail(request.getEmail());
        newUser.setIsActive(true);
        newUser.setRole(assignedRole);
        newUser.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        userRepository.save(newUser);

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getPhone());
        String accessToken = jwtUtil.generateAccessToken(userDetails);
        String refreshToken = jwtUtil.generateRefreshToken(userDetails);

        UserDto userDto = UserDto.builder()
                .id(newUser.getId())
                .name(newUser.getName())
                .role(assignedRole.getName())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(userDto)
                .build());
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        User user = userRepository.findByPhoneOrEmail(request.getLogin())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!user.getIsActive()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User account is disabled");
        }

        if (request.getPin() != null && user.getPinHash() != null) {
            if (!passwordEncoder.matches(request.getPin(), user.getPinHash())) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid PIN");
            }
        } else if (request.getPassword() != null && user.getPasswordHash() != null) {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getLogin(), request.getPassword())
            );
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password or PIN must be provided");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getLogin());
        
        String accessToken = jwtUtil.generateAccessToken(userDetails);
        String refreshToken = jwtUtil.generateRefreshToken(userDetails);

        String roleName = user.getRole() != null ? user.getRole().getName() : "NONE";

        UserDto userDto = UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .role(roleName)
                .build();

        return ResponseEntity.ok(AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(userDto)
                .build());
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody TokenRefreshRequest request) {
        String refreshToken = request.getRefreshToken();
        
        String username;
        try {
            username = jwtUtil.extractUsername(refreshToken);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        if (jwtUtil.validateToken(refreshToken, userDetails)) {
            String newAccessToken = jwtUtil.generateAccessToken(userDetails);
            
            return ResponseEntity.ok(AuthResponse.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(refreshToken)
                    .build());
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Expired or invalid refresh token");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        return ResponseEntity.ok("Logged out successfully");
    }
}
