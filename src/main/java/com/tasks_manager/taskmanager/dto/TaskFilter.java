package com.tasks_manager.taskmanager.dto;

public enum TaskFilter {
    ALL("Все"),
    ACTIVE("Активные"),
    TODAY("Сегодня"),
    WEEK("На неделе"),
    OVERDUE("Просроченные"),
    COMPLETED("Выполненные");

    private final String title;

    TaskFilter(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
