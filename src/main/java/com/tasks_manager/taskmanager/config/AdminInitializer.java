package com.tasks_manager.taskmanager.config;

import com.tasks_manager.taskmanager.entities.User;
import com.tasks_manager.taskmanager.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AdminInitializer implements ApplicationRunner{
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {

        if(!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@taskmanager.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRoles(new HashSet<>(Arrays.asList("ROLE_ADMIN", "ROLE_USER")));
            admin.setCreatedAt(LocalDateTime.now());
            admin.setUpdatedAt(LocalDateTime.now());
            userRepository.save(admin);
            System.out.println("Создан администратор: admin / admin123");
        }

        if (!userRepository.existsByUsername("test")) {
            User test = new User();
            test.setUsername("test");
            test.setEmail("test@taskmanager.com");
            test.setPassword(passwordEncoder.encode("test123"));
            test.setRoles(new HashSet<>(List.of("ROLE_USER")));
            test.setCreatedAt(LocalDateTime.now());
            test.setUpdatedAt(LocalDateTime.now());
            userRepository.save(test);
            System.out.println("Создан тестовый пользователь: test / test123");
        }

        if (!userRepository.existsByUsername("user")) {
            User user = new User();
            user.setUsername("user");
            user.setEmail("user@taskmanager.com");
            user.setPassword(passwordEncoder.encode("user123"));
            user.setRoles(new HashSet<>(List.of("ROLE_USER")));
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
            System.out.println("Создан пользователь: user / user123");
        }
    }
}
