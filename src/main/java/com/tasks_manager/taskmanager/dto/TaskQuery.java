package com.tasks_manager.taskmanager.dto;

import com.tasks_manager.taskmanager.entities.Priority;

public record TaskQuery(TaskFilter filter, Priority priority, String q, TaskSort sort) {

    public TaskQuery {
        filter = filter == null ? TaskFilter.ALL : filter;
        sort = sort == null ? TaskSort.NEWEST : sort;
        q = q == null || q.isBlank() ? null : q.strip();
    }

    public static TaskQuery all() {
        return new TaskQuery(null, null, null, null);
    }
}
