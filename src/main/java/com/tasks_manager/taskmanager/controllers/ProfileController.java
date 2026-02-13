package com.tasks_manager.taskmanager.controllers;

import com.tasks_manager.taskmanager.dto.ProfileForm;
import com.tasks_manager.taskmanager.security.AppUserPrincipal;
import com.tasks_manager.taskmanager.services.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final UserService userService;

    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String profile(@AuthenticationPrincipal AppUserPrincipal principal, Model model) {
        var user = userService.getUser(principal.id());
        model.addAttribute("form", new ProfileForm(user.getFirstName(), user.getLastName()));
        return "profile";
    }

    @PostMapping
    public String update(@AuthenticationPrincipal AppUserPrincipal principal,
                         @Valid @ModelAttribute("form") ProfileForm form, BindingResult result,
                         RedirectAttributes redirect) {
        if (result.hasErrors()) {
            return "profile";
        }
        userService.updateProfile(principal.id(), form);
        redirect.addFlashAttribute("message", "Профиль обновлён");
        return "redirect:/profile";
    }

}
