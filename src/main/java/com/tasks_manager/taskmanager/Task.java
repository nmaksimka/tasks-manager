package com.tasks_manager.taskmanager;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name="tasks")
@Data
@NoArgsConstructor
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Название задачи обязательно")
    @Size(min = 3, max = 100, message = "Название должно быть от 3 до 100 символов")
    @Column(nullable = false)
    private String title;

    @Size(max = 777, message = "Описание не может превышать 777 символов")
    @Column(columnDefinition = "TEXT")
    private String description;

    private boolean completed = false;

    private Integer priority = 2;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    public Task(String title, String description, Integer priority) {
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.completed = false;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
