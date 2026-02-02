package com.tasks_manager.taskmanager.services;

import com.tasks_manager.taskmanager.dto.TaskForm;
import com.tasks_manager.taskmanager.dto.TaskQuery;
import com.tasks_manager.taskmanager.dto.TaskResponse;
import com.tasks_manager.taskmanager.dto.TaskStats;
import com.tasks_manager.taskmanager.entities.Task;
import com.tasks_manager.taskmanager.exceptions.TaskNotFoundException;
import com.tasks_manager.taskmanager.repositories.TaskRepository;
import com.tasks_manager.taskmanager.repositories.TaskSpecifications;
import com.tasks_manager.taskmanager.repositories.UserRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final Clock clock;

    public TaskService(TaskRepository taskRepository, UserRepository userRepository, Clock clock) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.clock = clock;
    }

    public List<TaskResponse> findTasks(Long userId, TaskQuery query) {
        var today = today();
        return taskRepository.findAll(TaskSpecifications.matching(userId, query, today), query.sort().toSort())
                .stream()
                .map(task -> TaskResponse.from(task, today))
                .toList();
    }

    public TaskResponse getTask(Long userId, Long taskId) {
        return TaskResponse.from(findOwned(userId, taskId), today());
    }

    public TaskStats stats(Long userId) {
        var today = today();
        var total = taskRepository.countByUserId(userId);
        var completed = taskRepository.countByUserIdAndCompletedTrue(userId);
        return new TaskStats(
                total,
                total - completed,
                completed,
                taskRepository.countByUserIdAndCompletedFalseAndDeadline(userId, today),
                taskRepository.countByUserIdAndCompletedFalseAndDeadlineBefore(userId, today)
        );
    }

    @Transactional
    public TaskResponse createTask(Long userId, TaskForm form) {
        var task = new Task(userRepository.getReferenceById(userId), now());
        apply(task, form);
        return TaskResponse.from(taskRepository.save(task), today());
    }

    @Transactional
    public TaskResponse updateTask(Long userId, Long taskId, TaskForm form) {
        var task = findOwned(userId, taskId);
        apply(task, form);
        return TaskResponse.from(task, today());
    }

    @Transactional
    public TaskResponse completeTask(Long userId, Long taskId) {
        var task = findOwned(userId, taskId);
        if (!task.isCompleted()) {
            task.complete(now());
        }
        return TaskResponse.from(task, today());
    }

    @Transactional
    public TaskResponse reopenTask(Long userId, Long taskId) {
        var task = findOwned(userId, taskId);
        task.reopen();
        return TaskResponse.from(task, today());
    }

    @Transactional
    public TaskResponse toggleTask(Long userId, Long taskId) {
        var task = findOwned(userId, taskId);
        if (task.isCompleted()) {
            task.reopen();
        } else {
            task.complete(now());
        }
        return TaskResponse.from(task, today());
    }

    @Transactional
    public void deleteTask(Long userId, Long taskId) {
        taskRepository.delete(findOwned(userId, taskId));
    }

    private Task findOwned(Long userId, Long taskId) {
        return taskRepository.findByIdAndUserId(taskId, userId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));
    }

    private static void apply(Task task, TaskForm form) {
        task.setTitle(form.getTitle().strip());
        task.setDescription(form.getDescription() == null || form.getDescription().isBlank()
                ? null
                : form.getDescription().strip());
        task.setPriority(form.getPriority());
        task.setDeadline(form.getDeadline());
    }

    private LocalDate today() {
        return LocalDate.now(clock);
    }

    private LocalDateTime now() {
        return LocalDateTime.now(clock);
    }
}
