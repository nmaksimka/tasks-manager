package com.tasks_manager.taskmanager.dto;

public record TaskStats(long total, long active, long completed, long today, long week, long overdue) {

    public long count(TaskFilter filter) {
        return switch (filter) {
            case ALL -> total;
            case ACTIVE -> active;
            case COMPLETED -> completed;
            case TODAY -> today;
            case WEEK -> week;
            case OVERDUE -> overdue;
        };
    }

    public int completionPercent() {
        return total == 0 ? 0 : (int) (completed * 100 / total);
    }
}
