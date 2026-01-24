package com.tasks_manager.taskmanager;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;

    @PostConstruct
    public void initTestData() {
        if (taskRepository.count() == 0) {
            Task task1 = new Task("Тренировки",
                    "Сходить на 3 тренировки за эту неделю и выполнить весь план", 1);
            taskRepository.save(task1);

            Task task2 = new Task("Купить продукты",
                    "Молоко, хлеб, яйца, помидоры, лапша, куринне филе", 2);
            task2.setCompleted(true);
            task2.setCompletedAt(LocalDateTime.now().minusDays(1));
            taskRepository.save(task2);

            Task task3 = new Task("Сессия", "Сдать прогу, мкн и матан", 3);
            taskRepository.save(task3);

            System.out.println("Создано 3 тестовые задачи");
        }
    }

    public Task createTask(String title, String description, Integer priority) {
        Task task = new Task();
        task.setTitle(title);
        task.setDescription(description);
        task.setPriority(priority);
        task.setCompleted(false);

        return taskRepository.save(task);
    }

    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    public Task getTaskById(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));
    }

    public Task updateTask(Long id, String title, String description, Integer priority) {
        Task task = getTaskById(id);

        if(title != null) task.setTitle(title);
        if(description != null) task.setDescription(description);
        if(priority != null) task.setPriority(priority);

        return taskRepository.save(task);
    }

    public Task completeTask(Long id) {
        Task task = getTaskById(id);

        task.setCompleted(true);
        task.setCompletedAt(LocalDateTime.now());

        return taskRepository.save(task);
    }

    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }

    public List<Task> getCompletedTasks() {
        return taskRepository.findByCompleted(true);
    }

    public List<Task> getActiveTasks() {
        return taskRepository.findByCompleted(false);
    }

    public List<Task> searchTasks(String keyword) {
        if(keyword == null || keyword.trim().isEmpty()) {
            return getAllTasks();
        }
        String searchTerm = keyword.trim().toLowerCase();
        return getAllTasks().stream()
                .filter(task -> task.getTitle().toLowerCase().contains(searchTerm) ||
                        (task.getDescription() != null &&
                                task.getDescription().toLowerCase().contains(searchTerm)))
                .toList();
    }
}