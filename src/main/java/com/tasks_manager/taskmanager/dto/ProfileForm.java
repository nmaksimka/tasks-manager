package com.tasks_manager.taskmanager.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfileForm {

    @Size(max = 50, message = "Имя не длиннее 50 символов")
    private String firstName;

    @Size(max = 50, message = "Фамилия не длиннее 50 символов")
    private String lastName;
}
