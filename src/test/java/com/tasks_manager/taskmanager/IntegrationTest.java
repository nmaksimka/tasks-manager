package com.tasks_manager.taskmanager;

import com.tasks_manager.taskmanager.dto.RegistrationRequest;
import com.tasks_manager.taskmanager.repositories.TaskRepository;
import com.tasks_manager.taskmanager.repositories.UserRepository;
import com.tasks_manager.taskmanager.services.UserService;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.testcontainers.containers.PostgreSQLContainer;

@SpringBootTest(properties = "app.remember-me.key=test-key")
@Import(IntegrationTest.FixedClock.class)
public abstract class IntegrationTest {

    protected static final ZoneId ZONE = ZoneId.of("Asia/Tomsk");
    protected static final LocalDate TODAY = LocalDate.of(2026, 3, 10);

    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine");

    static {
        POSTGRES.start();
    }

    @Autowired
    protected UserService userService;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected TaskRepository taskRepository;

    @AfterEach
    void cleanDatabase() {
        taskRepository.deleteAllInBatch();
        userRepository.deleteAll();
    }

    protected Long register(String username) {
        var request = new RegistrationRequest();
        request.setUsername(username);
        request.setEmail(username + "@example.com");
        request.setPassword("password123");
        return userService.register(request).getId();
    }

    @TestConfiguration
    static class FixedClock {

        @Bean
        @Primary
        Clock fixedClock() {
            return Clock.fixed(TODAY.atTime(12, 0).atZone(ZONE).toInstant(), ZONE);
        }
    }
}
