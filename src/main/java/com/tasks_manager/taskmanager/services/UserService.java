package com.tasks_manager.taskmanager.services;

import com.tasks_manager.taskmanager.dto.ProfileForm;
import com.tasks_manager.taskmanager.dto.RegistrationRequest;
import com.tasks_manager.taskmanager.entities.User;
import com.tasks_manager.taskmanager.exceptions.UserAlreadyExistsException;
import com.tasks_manager.taskmanager.repositories.UserRepository;
import java.util.Locale;
import java.util.NoSuchElementException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User register(RegistrationRequest request) {
        var username = request.getUsername().strip();
        var email = request.getEmail().strip().toLowerCase(Locale.ROOT);
        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new UserAlreadyExistsException("username", "Логин уже занят");
        }
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new UserAlreadyExistsException("email", "Этот email уже зарегистрирован");
        }
        var user = new User(username, email, passwordEncoder.encode(request.getPassword()));
        user.setFirstName(blankToNull(request.getFirstName()));
        user.setLastName(blankToNull(request.getLastName()));
        try {
            return userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException e) {
            throw new UserAlreadyExistsException("username", "Логин или email уже заняты");
        }
    }

    public User getUser(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new NoSuchElementException("User not found: " + id));
    }

    @Transactional
    public User updateProfile(Long id, ProfileForm form) {
        var user = getUser(id);
        user.setFirstName(blankToNull(form.getFirstName()));
        user.setLastName(blankToNull(form.getLastName()));
        return user;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }
}
