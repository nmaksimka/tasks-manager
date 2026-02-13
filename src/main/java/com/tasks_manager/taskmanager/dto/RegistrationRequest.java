package com.tasks_manager.taskmanager.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegistrationRequest {

    @NotBlank(message = "Введите логин")
    @Size(min = 3, max = 50, message = "Логин от 3 до 50 символов")
    @Pattern(regexp = "[A-Za-z0-9_.-]*", message = "Логин: латиница, цифры, точка, дефис и подчёркивание")
    private String username;

    @NotBlank(message = "Введите email")
    @Email(message = "Неверный формат email")
    @Size(max = 255, message = "Email слишком длинный")
    private String email;

    @NotBlank(message = "Введите пароль")
    @Size(min = 8, max = 72, message = "Пароль от 8 до 72 символов")
    private String password;

    @Size(max = 50, message = "Имя не длиннее 50 символов")
    private String firstName;

    @Size(max = 50, message = "Фамилия не длиннее 50 символов")
    private String lastName;
}
