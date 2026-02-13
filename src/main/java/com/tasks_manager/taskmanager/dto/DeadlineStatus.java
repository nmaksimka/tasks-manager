package com.tasks_manager.taskmanager.dto;

import java.time.LocalDate;

public enum DeadlineStatus {
    NONE,
    DONE,
    OVERDUE,
    TODAY,
    SOON,
    LATER;

    private static final int SOON_DAYS = 3;

    public static DeadlineStatus of(LocalDate deadline, boolean completed, LocalDate today) {
        if (completed) {
            return DONE;
        }
        if (deadline == null) {
            return NONE;
        }
        if (deadline.isBefore(today)) {
            return OVERDUE;
        }
        if (deadline.isEqual(today)) {
            return TODAY;
        }
        return deadline.isAfter(today.plusDays(SOON_DAYS)) ? LATER : SOON;
    }
}
