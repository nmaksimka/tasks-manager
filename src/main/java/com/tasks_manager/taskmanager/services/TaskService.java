package com.tasks_manager.taskmanager.services;

import com.tasks_manager.taskmanager.entities.*;
import com.tasks_manager.taskmanager.repositories.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;
    private final UserService userService;
    private final UserRepository userRepository;

    @PostConstruct
    public void initTestData() {
        if (taskRepository.count() == 0) {
            User testUser = userRepository.findByUsername("test").orElseGet(() -> {
               User user = new User();
               user.setUsername("test");
               user.setEmail("test@example.com");
               String encodedPassword = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode("password");
               user.setPassword(encodedPassword);
               user.setRoles(java.util.Collections.singleton("ROLE_USER"));
               user.setCreatedAt(LocalDateTime.now());
               user.setUpdatedAt(LocalDateTime.now());
               return userRepository.save(user);
            });
            Task task1 = new Task("Тренировки",
                    "Сходить на 3 тренировки за эту неделю и выполнить весь план", 1);
            task1.setDeadline(LocalDate.now().plusDays(3));
            task1.setUser(testUser);
            taskRepository.save(task1);

            Task task2 = new Task("Купить продукты",
                    "Молоко, хлеб, яйца, помидоры, лапша, куринне филе", 2);
            task2.setCompleted(true);
            task2.setUser(testUser);
            task2.setCompletedAt(LocalDateTime.now().minusDays(1));
            task2.setDeadline(LocalDate.now().minusDays(1));
            taskRepository.save(task2);

            Task task3 = new Task("Сессия", "Сдать прогу, мкн и матан", 3);
            task3.setDeadline(LocalDate.now().plusWeeks(2));
            task3.setUser(testUser);
            taskRepository.save(task3);

            System.out.println("Создано 3 тестовые задачи пользователя " + testUser.getUsername());
        }
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Пользователь не аутентифицирован");
        }

        String username = ((UserDetails) authentication.getPrincipal()).getUsername();
        return userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("Пользователь не найден"));
    }

    public Task createTask(String title, String description, Integer priority, LocalDate deadline) {
        User currentUser = getCurrentUser();

        Task task = new Task();
        task.setTitle(title);
        task.setDescription(description);
        task.setPriority(priority);
        task.setCompleted(false);
        task.setDeadline(deadline);
        task.setUser(currentUser);

        return taskRepository.save(task);
    }

    public List<Task> getAllTasks() {
        User currentUser = getCurrentUser();
        return taskRepository.findByUserId(currentUser.getId());
    }

    public Task getTaskById(Long id) {
        User currentUser = getCurrentUser();
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        if(!task.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Доступ запрещен: информация принадлежит другому пользователю");
        }
        return task;
    }

    public Task updateTask(Long id, String title, String description, Integer priority, LocalDate deadline) {
        User currentUser = getCurrentUser();
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        if (!task.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Доступ запрещен: информация принадлежит другому пользователю");
        }

        if(title != null) task.setTitle(title);
        if(description != null) task.setDescription(description);
        if(priority != null) task.setPriority(priority);
        if(deadline != null) task.setDeadline(deadline);

        return taskRepository.save(task);
    }

    public Task completeTask(Long id) {
        User currentUser = getCurrentUser();
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        if (!task.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Доступ запрещен: информация принадлежит другому пользователю");
        }

        task.setCompleted(true);
        task.setCompletedAt(LocalDateTime.now());

        return taskRepository.save(task);
    }

    public void deleteTask(Long id) {
        User currentUser = getCurrentUser();
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        if (!task.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Доступ запрещен: информация принадлежит другому пользователю");
        }
        taskRepository.deleteById(id);
    }

    public List<Task> getCompletedTasks() {
        User currentUser = getCurrentUser();
        return taskRepository.findByUserIdAndCompletedTrue(currentUser.getId());
    }

    public List<Task> getActiveTasks() {
        User currentUser = getCurrentUser();
        return taskRepository.findByUserIdAndCompletedFalse(currentUser.getId());
    }

    public List<Task> searchTasks(String keyword) {
        User currentUser = getCurrentUser();
        if(keyword == null || keyword.trim().isEmpty()) {
            return getAllTasks();
        }
        return taskRepository.searchByUser(currentUser.getId(), keyword.trim().toLowerCase());
    }

    public List<Task> getOverdueTasks() {
        User currentUser = getCurrentUser();
        LocalDate today = LocalDate.now();
        return taskRepository.findByUserIdAndDeadlineBeforeAndCompletedFalse(currentUser.getId(), today);
    }

    public List<Task> getTodayTasks() {
        User currentUser = getCurrentUser();
        LocalDate today = LocalDate.now();
        LocalDate endOfWeek = LocalDate.now().plusWeeks(1);
        return taskRepository.findByUserIdAndDeadlineAndCompletedFalse(currentUser.getId(), today);
    }

    public List<Task> getTasksForThisWeek() {
        User currentUser = getCurrentUser();
        LocalDate today = LocalDate.now();
        LocalDate endOfWeek = LocalDate.now().plusWeeks(1);
        return taskRepository.findByUserIdAndDeadlineBetweenAndCompletedFalse(currentUser.getId(), today, endOfWeek);
    }

    public List<Task> getTasksByPriority(Integer priority) {
        User currentUser = getCurrentUser();
        return taskRepository.findByUserIdAndPriority(currentUser.getId(), priority);
    }
}