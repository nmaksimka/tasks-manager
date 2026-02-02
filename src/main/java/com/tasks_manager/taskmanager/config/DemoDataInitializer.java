package com.tasks_manager.taskmanager.config;

import com.tasks_manager.taskmanager.dto.RegistrationRequest;
import com.tasks_manager.taskmanager.dto.TaskForm;
import com.tasks_manager.taskmanager.entities.Priority;
import com.tasks_manager.taskmanager.repositories.UserRepository;
import com.tasks_manager.taskmanager.services.TaskService;
import com.tasks_manager.taskmanager.services.UserService;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
public class DemoDataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataInitializer.class);
    private static final String DEMO_USERNAME = "demo";

    private final UserRepository userRepository;
    private final UserService userService;
    private final TaskService taskService;
    private final Clock clock;
    private final String demoPassword;

    public DemoDataInitializer(UserRepository userRepository, UserService userService, TaskService taskService,
                               Clock clock, @Value("${app.demo.password}") String demoPassword) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.taskService = taskService;
        this.clock = clock;
        this.demoPassword = demoPassword;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.findByUsername(DEMO_USERNAME).isPresent()) {
            return;
        }
        var request = new RegistrationRequest();
        request.setUsername(DEMO_USERNAME);
        request.setEmail("demo@example.com");
        request.setPassword(demoPassword);
        request.setFirstName("Демо");
        request.setLastName("Пользователь");
        var userId = userService.register(request).getId();

        var today = LocalDate.now(clock);
        List.of(
                new TaskForm("Сдать лабораторную по БД", "Нормализация до 3НФ и отчёт", Priority.HIGH, today),
                new TaskForm("Тренировка", "Три подхода, план на неделю", Priority.MEDIUM, today.plusDays(2)),
                new TaskForm("Купить продукты", "Молоко, хлеб, яйца, курица", Priority.LOW, today.minusDays(1)),
                new TaskForm("Подготовиться к собеседованию", "Коллекции, Stream API, транзакции",
                        Priority.HIGH, today.plusDays(6)),
                new TaskForm("Прочитать главу про JPA", null, Priority.LOW, null)
        ).forEach(form -> taskService.createTask(userId, form));
        var done = taskService.createTask(userId,
                new TaskForm("Настроить проект", "Spring Boot, PostgreSQL, Flyway", Priority.MEDIUM, today.minusDays(3)));
        taskService.completeTask(userId, done.id());
        log.info("Demo user '{}' created", DEMO_USERNAME);
    }
}
