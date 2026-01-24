package com.tasks_manager.taskmanager.dto;

import lombok.Data;

@Data

public class TaskCreateRequest {
    private String title;
    private String description;
    private Integer priority = 2;
}
