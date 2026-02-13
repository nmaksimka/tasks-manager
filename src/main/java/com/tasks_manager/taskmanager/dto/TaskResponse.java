package com.tasks_manager.taskmanager.dto;

import com.tasks_manager.taskmanager.entities.Priority;
import com.tasks_manager.taskmanager.entities.Task;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record TaskResponse(
        Long id,
        String title,
        String description,
        Priority priority,
        boolean completed,
        LocalDate deadline,
        DeadlineStatus deadlineStatus,
        LocalDateTime createdAt,
        LocalDateTime completedAt
) {

    public static TaskResponse from(Task task, LocalDate today) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getPriority(),
                task.isCompleted(),
                task.getDeadline(),
                DeadlineStatus.of(task.getDeadline(), task.isCompleted(), today),
                task.getCreatedAt(),
                task.getCompletedAt()
        );
    }
}
