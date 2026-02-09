package com.tasks_manager.taskmanager.web;

import com.tasks_manager.taskmanager.controllers.ProfileController;
import com.tasks_manager.taskmanager.controllers.TaskWebController;
import com.tasks_manager.taskmanager.dto.TaskFilter;
import com.tasks_manager.taskmanager.dto.TaskQuery;
import com.tasks_manager.taskmanager.dto.TaskSort;
import com.tasks_manager.taskmanager.entities.Priority;
import com.tasks_manager.taskmanager.security.AppUserPrincipal;
import com.tasks_manager.taskmanager.services.TaskService;
import com.tasks_manager.taskmanager.services.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice(assignableTypes = {TaskWebController.class, ProfileController.class})
public class SidebarModelAdvice {

    private final TaskService taskService;
    private final UserService userService;

    public SidebarModelAdvice(TaskService taskService, UserService userService) {
        this.taskService = taskService;
        this.userService = userService;
    }

    @ModelAttribute
    void sidebar(@AuthenticationPrincipal AppUserPrincipal principal, Model model) {
        model.addAttribute("priorities", Priority.values());
        model.addAttribute("filters", TaskFilter.values());
        model.addAttribute("sorts", TaskSort.values());
        TaskLinks.addTo(model, TaskQuery.all());
        if (principal != null) {
            model.addAttribute("me", userService.getUser(principal.id()));
            model.addAttribute("stats", taskService.stats(principal.id()));
        }
    }
}
