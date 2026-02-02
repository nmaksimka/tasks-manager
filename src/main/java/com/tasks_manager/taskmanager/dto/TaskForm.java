package com.tasks_manager.taskmanager.dto;

import com.tasks_manager.taskmanager.entities.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskForm {

    @NotBlank(message = "Введите название задачи")
    @Size(max = 100, message = "Название не длиннее 100 символов")
    private String title;

    @Size(max = 2000, message = "Описание не длиннее 2000 символов")
    private String description;

    @NotNull(message = "Выберите приоритет")
    private Priority priority = Priority.MEDIUM;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate deadline;

    public static TaskForm from(TaskResponse task) {
        return new TaskForm(task.title(), task.description(), task.priority(), task.deadline());
    }
}
