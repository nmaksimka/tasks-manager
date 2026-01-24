package com.tasks_manager.taskmanager;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class WebController {
    private final TaskService taskService;

    @GetMapping("/debug")
    @ResponseBody
    public String debug() {
        return "WebController работает! Количество задач: " + taskService.getAllTasks().size();
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("tasks", taskService.getAllTasks());
        model.addAttribute("activeTasks", taskService.getActiveTasks());
        model.addAttribute("completedTasks", taskService.getCompletedTasks());
        return "index";
    }

    @GetMapping("/tasks/new")
    public String showCreateForm(Model model) {
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
        if (task.getId() == null) {
            taskService.createTask(task.getTitle(), task.getDescription(), task.getPriority());
        } else {
            taskService.updateTask(task.getId(), task.getTitle(), task.getDescription(), task.getPriority());
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
                tasks.sort((t1, t2) -> t2.getCreatedAt().compareTo(t1.getCreatedAt())); // новые сверху
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
        List<Task> tasks;

        switch (filter) {
            case "active":
                tasks = taskService.getActiveTasks();
                break;
            case "completed":
                tasks = taskService.getCompletedTasks();
                break;
            case "high":
                tasks = taskService.getAllTasks().stream()
                        .filter(task -> task.getPriority() == 1)
                        .toList();
                break;
            case "medium":
                tasks = taskService.getAllTasks().stream()
                        .filter(task -> task.getPriority() == 2)
                        .toList();
                break;
            case "low":
                tasks = taskService.getAllTasks().stream()
                        .filter(task -> task.getPriority() == 3)
                        .toList();
                break;
            default:
                tasks = taskService.getAllTasks();
        }
        model.addAttribute("tasks", tasks);
        model.addAttribute("activeFilter", filter);
        model.addAttribute("activeTasks", taskService.getActiveTasks());
        model.addAttribute("completedTasks", taskService.getCompletedTasks());
        return "index";
    }
}
