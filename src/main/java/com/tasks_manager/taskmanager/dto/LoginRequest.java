package com.tasks_manager.taskmanager.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

@Data
public class LoginRequest {
    @NotBlank(message = "Имя пользователя или email обязателен")
    private String usernameOrEmail;

    @NotBlank(message = "Пароль обязателен")
    private String password;
}
