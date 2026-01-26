package com.tasks_manager.taskmanager.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class TaskUpdateRequest {
    private String title;
    private String description;
    private Integer priority;
    private LocalDate deadline;
}
