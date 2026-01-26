package com.tasks_manager.taskmanager.controllers;

import com.tasks_manager.taskmanager.dto.LoginRequest;
import com.tasks_manager.taskmanager.dto.RegistrationRequest;
import com.tasks_manager.taskmanager.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;

    @GetMapping("/login")
    public String showLoginForm(Model model) {
        model.addAttribute("loginRequest", new LoginRequest());
        return "login";
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("registrationRequest", new RegistrationRequest());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute RegistrationRequest request, BindingResult result, Model model) {
        if(result.hasErrors()) {
            return "register";
        }

        try {
            userService.registerUser(
                    request.getUsername(),
                    request.getEmail(),
                    request.getPassword()
            );
            return "redirect:/login?registered";
        } catch (RuntimeException err) {
            model.addAttribute("error", err.getMessage());
            return "register";
        }
    }
}
