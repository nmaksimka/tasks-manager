package com.tasks_manager.taskmanager.controllers;

import com.tasks_manager.taskmanager.dto.RegistrationRequest;
import com.tasks_manager.taskmanager.exceptions.UserAlreadyExistsException;
import com.tasks_manager.taskmanager.services.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String registerForm(@ModelAttribute("form") RegistrationRequest form) {
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("form") RegistrationRequest form, BindingResult result) {
        if (result.hasErrors()) {
            return "register";
        }
        try {
            userService.register(form);
        } catch (UserAlreadyExistsException e) {
            result.rejectValue(e.getField(), "duplicate", e.getMessage());
            return "register";
        }
        return "redirect:/login?registered";
    }
}
