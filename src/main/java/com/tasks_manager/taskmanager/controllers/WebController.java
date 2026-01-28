package com.tasks_manager.taskmanager.controllers;

import com.tasks_manager.taskmanager.entities.*;
import com.tasks_manager.taskmanager.services.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class WebController {
    private final UserService userService;
    private final TaskService taskService;

    @GetMapping("/debug")
    @ResponseBody
    public String debug() {
        return "WebController работает! Количество задач: " + taskService.getAllTasks().size();
    }

    @GetMapping("/")
    public String index(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if(userDetails != null) {
            User user = userService.findByUsername(userDetails.getUsername());
            model.addAttribute("activeTasks", taskService.getActiveTasks());
            model.addAttribute("completedTasks", taskService.getCompletedTasks());
            model.addAttribute("tasks", taskService.getAllTasks());
            model.addAttribute("username", user.getUsername());
        } else {
            // Для неаутентифицированных пользователей добавляем пустые списки
            model.addAttribute("activeTasks", List.of());
            model.addAttribute("completedTasks", List.of());
            model.addAttribute("tasks", List.of());
        }
        return "index";
    }

    @GetMapping("/tasks/new")
    public String showCreateForm(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        model.addAttribute("task", new Task());
        model.addAttribute("isEdit", false);
        return "task-form";
    }

    @GetMapping("/tasks/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Task task = taskService.getTaskById(id);
        model.addAttribute("task", task);
        model.addAttribute("isEdit", true);
        return "task-form";
    }

    @PostMapping("/tasks/save")
    public String saveTask(@ModelAttribute Task task) {
        if(task.getId() != null) {
            taskService.updateTask(task.getId(), task.getTitle(), task.getDescription(), task.getPriority(), task.getDeadline());
        } else {
            taskService.createTask(task.getTitle(), task.getDescription(), task.getPriority(), task.getDeadline());
        }
        return "redirect:/";
    }

    @GetMapping("/tasks/complete/{id}")
    public String completeTask(@PathVariable Long id) {
        taskService.completeTask(id);
        return "redirect:/";
    }

    @GetMapping("/tasks/delete/{id}")
    public String deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return "redirect:/";
    }

    @GetMapping("/tasks/search")
    public String searchTasks(@RequestParam(required = false) String keyword, Model model) {
        List<Task> tasks = taskService.searchTasks(keyword);
        model.addAttribute("tasks", tasks);
        model.addAttribute("searchKeyword", keyword);
        model.addAttribute("activeTasks", taskService.getActiveTasks());
        model.addAttribute("completedTasks", taskService.getCompletedTasks());
        return "index";
    }

    @GetMapping("/tasks/sort")
    public String sortTasks(@RequestParam String by, Model model) {
        var tasks = taskService.getAllTasks();

        switch (by) {
            case "priority":
                tasks.sort((t1, t2) -> t1.getPriority().compareTo(t2.getPriority()));
                break;
            case "createdAt":
                tasks.sort((t1, t2) -> t2.getCreatedAt().compareTo(t1.getCreatedAt()));
                break;
            case "title":
                tasks.sort((t1, t2) -> t1.getTitle().compareToIgnoreCase(t2.getTitle()));
                break;
        }

        model.addAttribute("tasks", tasks);
        model.addAttribute("sortBy", by);
        model.addAttribute("activeTasks", taskService.getActiveTasks());
        model.addAttribute("completedTasks", taskService.getCompletedTasks());
        return "index";
    }

    @GetMapping("/tasks/filter")
    public String filterTasks(@RequestParam String filter, Model model) {
        List<Task> tasks = switch (filter) {
            case "active" -> taskService.getActiveTasks();
            case "completed" -> taskService.getCompletedTasks();
            case "high" -> taskService.getAllTasks().stream()
                    .filter(task -> task.getPriority() == 1)
                    .toList();
            case "medium" -> taskService.getAllTasks().stream()
                    .filter(task -> task.getPriority() == 2)
                    .toList();
            case "low" -> taskService.getAllTasks().stream()
                    .filter(task -> task.getPriority() == 3)
                    .toList();
            default -> taskService.getAllTasks();
        };

        model.addAttribute("tasks", tasks);
        model.addAttribute("activeFilter", filter);
        model.addAttribute("activeTasks", taskService.getActiveTasks());
        model.addAttribute("completedTasks", taskService.getCompletedTasks());
        return "index";
    }

    @GetMapping("/tasks/deadline")
    public String filterByDeadline(@RequestParam String type, Model model) {
        List<Task> tasks;
        LocalDate today = LocalDate.now();

        tasks = switch (type) {
            case "overdue" -> taskService.getOverdueTasks();
            case "today" -> taskService.getTodayTasks();
            case "week" -> taskService.getAllTasks().stream()
                    .filter(task -> task.getDeadline() != null
                            && !task.isCompleted()
                            && !task.getDeadline().isBefore(today)
                            && !task.getDeadline().isAfter(today.plusWeeks(1)))
                    .toList();
            default -> taskService.getAllTasks();
        };

        model.addAttribute("tasks", tasks);
        model.addAttribute("deadlineFilter", type);
        model.addAttribute("activeTasks", taskService.getActiveTasks());
        model.addAttribute("completedTasks", taskService.getCompletedTasks());
        return "index";
    }

    @GetMapping("/profile")
    public String showProfile(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if(userDetails != null) {
            User user = userService.findByUsername(userDetails.getUsername());
            model.addAttribute("user", user);
            model.addAttribute("taskCount", taskService.getAllTasks().size());
            model.addAttribute("activeTaskCount", taskService.getActiveTasks().size());
            model.addAttribute("completedTaskCount", taskService.getCompletedTasks().size());
            return "profile";
        }
        return "redirect:/login";
    }
}