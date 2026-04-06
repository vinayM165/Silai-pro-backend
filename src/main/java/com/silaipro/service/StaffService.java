package com.silaipro.service;

import com.silaipro.dto.user.PasswordResetRequest;
import com.silaipro.dto.user.StaffRequest;
import com.silaipro.dto.user.StaffResponse;
import com.silaipro.dto.user.StaffWorkReport;
import com.silaipro.entity.*;
import com.silaipro.enums.OrderStatus;
import com.silaipro.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StaffService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AuditLogRepository auditLogRepository;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    @Transactional
    public StaffResponse createStaff(StaffRequest request) {
        if (userRepository.findByPhone(request.getPhone()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Phone number already exists");
        }

        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found"));

        User user = User.builder()
                .name(request.getName())
                .phone(request.getPhone())
                .email(request.getEmail())
                .role(role)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .pinHash(request.getPin() != null ? passwordEncoder.encode(request.getPin()) : null)
                .isActive(true)
                .build();

        user = userRepository.save(user);
        return mapToResponse(user);
    }

    @Transactional
    public StaffResponse updateStaff(Long id, StaffRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Staff member not found"));

        user.setName(request.getName());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());

        if (request.getRoleId() != null) {
            Role role = roleRepository.findById(request.getRoleId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found"));
            user.setRole(role);
        }

        user = userRepository.save(user);
        return mapToResponse(user);
    }

    @Transactional
    public void deactivateStaff(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Staff member not found"));
        user.setIsActive(false);
        userRepository.save(user);
    }

    @Transactional
    public void resetPassword(Long id, PasswordResetRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Staff member not found"));
        
        if (request.getNewPassword() != null) {
            user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        }
        if (request.getNewPin() != null) {
            user.setPinHash(passwordEncoder.encode(request.getNewPin()));
        }
        
        userRepository.save(user);
    }

    @Transactional
    public void assignRole(Long id, Long roleId) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Staff member not found"));
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found"));
        user.setRole(role);
        userRepository.save(user);
    }

    public Page<StaffResponse> getAllStaff(Pageable pageable) {
        return userRepository.findAll(pageable).map(this::mapToResponse);
    }

    public StaffResponse getStaffById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Staff member not found"));
        return mapToResponse(user);
    }

    public List<AuditLog> getLoginHistory(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Staff member not found"));
        
        return auditLogRepository.findByPerformedByAndAction(user, "LOGIN");
    }

    public StaffWorkReport getStaffWorkReport(Long id, LocalDate from, LocalDate to) {
        User staff = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Staff member not found"));

        LocalDateTime fromDt = from.atStartOfDay();
        LocalDateTime toDt = to.atTime(23, 59, 59);

        // Orders handled (assigned to)
        long ordersHandled = orderRepository.findByAssignedToIdAndCreatedAtBetween(id, fromDt, toDt).size();
        
        // Delivered orders
        long deliveredOrders = orderRepository.findByAssignedToIdAndCreatedAtBetween(id, fromDt, toDt).stream()
                .filter(o -> o.getStatus() == OrderStatus.DELIVERED)
                .count();

        // Amount collected (received by)
        BigDecimal totalCollected = paymentRepository.findByReceivedByIdAndDateRange(id, from, to).stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Revenue generated (revenue from orders assigned to staff)
        // This is a simplified calculation: total amount of orders assigned to them in this range.
        BigDecimal revenueGenerated = orderRepository.findByAssignedToIdAndCreatedAtBetween(id, fromDt, toDt).stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return StaffWorkReport.builder()
                .staffId(id)
                .staffName(staff.getName())
                .ordersHandled(ordersHandled)
                .deliveredOrders(deliveredOrders)
                .revenueGenerated(revenueGenerated)
                .totalCollected(totalCollected)
                .build();
    }

    private StaffResponse mapToResponse(User user) {
        return StaffResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .phone(user.getPhone())
                .email(user.getEmail())
                .roleName(user.getRole() != null ? user.getRole().getName() : "NONE")
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
