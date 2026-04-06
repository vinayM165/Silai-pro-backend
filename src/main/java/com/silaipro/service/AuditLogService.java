package com.silaipro.service;

import com.silaipro.entity.AuditLog;
import com.silaipro.entity.User;
import com.silaipro.repository.AuditLogRepository;
import com.silaipro.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(String action, String entityType, Long entityId, String oldValue, String newValue) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User performer = userRepository.findByPhoneOrEmail(currentUsername).orElse(null);

        AuditLog log = AuditLog.builder()
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .oldValue(oldValue)
                .newValue(newValue)
                .performedBy(performer)
                .performedAt(LocalDateTime.now())
                .build();

        auditLogRepository.save(log);
    }
}
