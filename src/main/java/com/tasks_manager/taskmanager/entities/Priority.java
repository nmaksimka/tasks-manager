package com.tasks_manager.taskmanager.entities;

import java.util.Arrays;

public enum Priority {
    HIGH(1, "Высокий"),
    MEDIUM(2, "Средний"),
    LOW(3, "Низкий");

    private final int code;
    private final String title;

    Priority(int code, String title) {
        this.code = code;
        this.title = title;
    }

    public int getCode() {
        return code;
    }

    public String getTitle() {
        return title;
    }

    public static Priority fromCode(int code) {
        return Arrays.stream(values())
                .filter(priority -> priority.code == code)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown priority code: " + code));
    }
}
