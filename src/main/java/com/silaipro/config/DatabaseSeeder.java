package com.silaipro.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.silaipro.constant.PermissionConstants;
import com.silaipro.entity.Role;
import com.silaipro.entity.User;
import com.silaipro.repository.RoleRepository;
import com.silaipro.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        seedRoles();
        seedAdminUser();
    }

    private void seedRoles() throws Exception {
        seedRoleIfAbsent("Admin",     Arrays.asList(PermissionConstants.ALL_PERMISSIONS));
        seedRoleIfAbsent("Manager",   Arrays.asList(PermissionConstants.MANAGER_PERMISSIONS));
        seedRoleIfAbsent("Staff",     Arrays.asList(PermissionConstants.STAFF_PERMISSIONS));
        seedRoleIfAbsent("View Only", Arrays.asList(PermissionConstants.VIEW_ONLY_PERMISSIONS));
    }

    private void seedRoleIfAbsent(String roleName, List<String> permissions) throws Exception {
        if (roleRepository.findByName(roleName).isEmpty()) {
            Role role = new Role();
            role.setName(roleName);
            role.setPermissionsJson(objectMapper.writeValueAsString(permissions));
            roleRepository.save(role);
            log.info("✅ Role '{}' seeded with {} permissions.", roleName, permissions.size());
        }
    }

    private void seedAdminUser() {
        if (userRepository.count() == 0) {
            roleRepository.findByName("Admin").ifPresent(adminRole -> {
                User admin = new User();
                admin.setName("System Admin");
                admin.setPhone("9999999999");
                admin.setEmail("admin@silaipro.com");
                admin.setRole(adminRole);
                admin.setPasswordHash(passwordEncoder.encode("admin123"));
                admin.setPinHash(passwordEncoder.encode("1234"));
                admin.setIsActive(true);
                userRepository.save(admin);
                log.info("✅ Default admin created. Login: 9999999999 / Password: admin123 / PIN: 1234");
            });
        }
    }
}
