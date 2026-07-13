package com.silaipro;

import com.silaipro.entity.User;
import com.silaipro.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

@SpringBootTest
@ActiveProfiles("dev")
@org.junit.jupiter.api.Disabled("Skipped during automated builds because it requires active database connection")
public class DbDumpTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    public void dumpUsers() {
        System.out.println("=== DUMPING USERS ===");
        List<User> users = userRepository.findAll();
        for (User u : users) {
            System.out.println("User: id=" + u.getId() + ", name=" + u.getName() + ", phone=" + u.getPhone() + ", email=" + u.getEmail() + ", active=" + u.getIsActive() + ", role=" + u.getRole().getName());
        }
        System.out.println("=====================");
    }
}
