package com.tasks_manager.taskmanager.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tasks_manager.taskmanager.IntegrationTest;
import com.tasks_manager.taskmanager.dto.ProfileForm;
import com.tasks_manager.taskmanager.dto.RegistrationRequest;
import com.tasks_manager.taskmanager.entities.User;
import com.tasks_manager.taskmanager.exceptions.UserAlreadyExistsException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

class UserServiceIT extends IntegrationTest {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void registrationNormalizesEmailAndHashesPassword() {
        var user = userService.register(request(" Max ", " Max@Example.COM ", "Иван"));

        assertThat(user.getUsername()).isEqualTo("Max");
        assertThat(user.getEmail()).isEqualTo("max@example.com");
        assertThat(user.getPassword()).isNotEqualTo("password123").startsWith("{bcrypt}");
        assertThat(passwordEncoder.matches("password123", user.getPassword())).isTrue();
        assertThat(user.getRoles()).containsExactly(User.ROLE_USER);
        assertThat(user.getDisplayName()).isEqualTo("Иван");
    }

    @Test
    void usernameIsUniqueIgnoringCase() {
        userService.register(request("max", "max@example.com", null));

        assertThatThrownBy(() -> userService.register(request("MAX", "other@example.com", null)))
                .isInstanceOf(UserAlreadyExistsException.class)
                .extracting("field").isEqualTo("username");
    }

    @Test
    void emailIsUniqueIgnoringCase() {
        userService.register(request("max", "max@example.com", null));

        assertThatThrownBy(() -> userService.register(request("other", "MAX@example.com", null)))
                .isInstanceOf(UserAlreadyExistsException.class)
                .extracting("field").isEqualTo("email");
    }

    @Test
    void profileUpdateStoresBlankAsNull() {
        var id = register("max");

        var user = userService.updateProfile(id, new ProfileForm("  Максим ", "   "));

        assertThat(user.getFirstName()).isEqualTo("Максим");
        assertThat(user.getLastName()).isNull();
        assertThat(user.getDisplayName()).isEqualTo("Максим");
    }

    @Test
    void displayNameFallsBackToUsername() {
        var id = register("max");

        assertThat(userService.getUser(id).getDisplayName()).isEqualTo("max");
    }

    private static RegistrationRequest request(String username, String email, String firstName) {
        var request = new RegistrationRequest();
        request.setUsername(username);
        request.setEmail(email);
        request.setPassword("password123");
        request.setFirstName(firstName);
        return request;
    }
}
