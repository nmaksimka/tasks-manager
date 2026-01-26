package com.tasks_manager.taskmanager.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class TaskCreateRequest {
    private String title;
    private String description;
    private Integer priority = 2;
    private LocalDate deadline;
}
