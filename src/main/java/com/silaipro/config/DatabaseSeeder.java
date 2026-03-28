package com.silaipro.config;

import com.silaipro.entity.Role;
import com.silaipro.entity.User;
import com.silaipro.repository.RoleRepository;
import com.silaipro.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (roleRepository.count() == 0) {
            Role adminRole = new Role();
            adminRole.setName("Admin");
            adminRole.setPermissionsJson("[\"ALL_PERMISSIONS\"]");
            roleRepository.save(adminRole);
            System.out.println("✅ Admin Role seeded.");

            if (userRepository.count() == 0) {
                User admin = new User();
                admin.setName("System Admin");
                admin.setPhone("9876543210");
                admin.setEmail("admin@silaipro.com");
                admin.setRole(adminRole);
                admin.setPasswordHash(passwordEncoder.encode("admin123"));
                admin.setIsActive(true);
                userRepository.save(admin);
                System.out.println("✅ Default System Admin created! Login: 9876543210 / Password: admin123");
            }
        }
    }
}
