package com.tasks_manager.taskmanager.dto;

public record TaskStats(long total, long active, long completed, long today, long overdue) {

    public int completionPercent() {
        return total == 0 ? 0 : (int) (completed * 100 / total);
    }
}
