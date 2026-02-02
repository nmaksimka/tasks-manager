package com.tasks_manager.taskmanager.exceptions;

public class UserAlreadyExistsException extends RuntimeException {

    private final String field;

    public UserAlreadyExistsException(String field, String message) {
        super(message);
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
