package com.tasks_manager.taskmanager.controllers;

import com.tasks_manager.taskmanager.dto.TaskFilter;
import com.tasks_manager.taskmanager.dto.TaskForm;
import com.tasks_manager.taskmanager.dto.TaskQuery;
import com.tasks_manager.taskmanager.dto.TaskSort;
import com.tasks_manager.taskmanager.security.AppUserPrincipal;
import com.tasks_manager.taskmanager.services.TaskService;
import com.tasks_manager.taskmanager.web.TaskLinks;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class TaskWebController {

    private final TaskService taskService;

    public TaskWebController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping("/")
    public String index(@AuthenticationPrincipal AppUserPrincipal user, @ModelAttribute("query") TaskQuery query,
                        Model model) {
        model.addAttribute("tasks", taskService.findTasks(user.id(), query));
        TaskLinks.addTo(model, query);
        return "index";
    }

    @GetMapping("/tasks/new")
    public String newTask(@ModelAttribute("form") TaskForm form) {
        return "task-form";
    }

    @PostMapping("/tasks")
    public String create(@AuthenticationPrincipal AppUserPrincipal user, @Valid @ModelAttribute("form") TaskForm form,
                         BindingResult result, RedirectAttributes redirect) {
        if (result.hasErrors()) {
            return "task-form";
        }
        var task = taskService.createTask(user.id(), form);
        redirect.addFlashAttribute("message", "Задача «" + task.title() + "» добавлена");
        return "redirect:/";
    }

    @GetMapping("/tasks/{id}/edit")
    public String edit(@AuthenticationPrincipal AppUserPrincipal user, @PathVariable Long id, Model model) {
        var task = taskService.getTask(user.id(), id);
        model.addAttribute("task", task);
        model.addAttribute("form", TaskForm.from(task));
        return "task-form";
    }

    @PostMapping("/tasks/{id}")
    public String update(@AuthenticationPrincipal AppUserPrincipal user, @PathVariable Long id,
                         @Valid @ModelAttribute("form") TaskForm form, BindingResult result, Model model,
                         RedirectAttributes redirect) {
        if (result.hasErrors()) {
            model.addAttribute("task", taskService.getTask(user.id(), id));
            return "task-form";
        }
        taskService.updateTask(user.id(), id, form);
        redirect.addFlashAttribute("message", "Изменения сохранены");
        return "redirect:/";
    }

    @PostMapping("/tasks/{id}/toggle")
    public String toggle(@AuthenticationPrincipal AppUserPrincipal user, @PathVariable Long id,
                         @ModelAttribute("query") TaskQuery query, RedirectAttributes redirect) {
        taskService.toggleTask(user.id(), id);
        keepQuery(query, redirect);
        return "redirect:/";
    }

    @PostMapping("/tasks/{id}/delete")
    public String delete(@AuthenticationPrincipal AppUserPrincipal user, @PathVariable Long id,
                         RedirectAttributes redirect) {
        taskService.deleteTask(user.id(), id);
        redirect.addFlashAttribute("message", "Задача удалена");
        return "redirect:/";
    }

    private static void keepQuery(TaskQuery query, RedirectAttributes redirect) {
        if (query.filter() != TaskFilter.ALL) {
            redirect.addAttribute("filter", query.filter());
        }
        if (query.priority() != null) {
            redirect.addAttribute("priority", query.priority());
        }
        if (query.q() != null) {
            redirect.addAttribute("q", query.q());
        }
        if (query.sort() != TaskSort.NEWEST) {
            redirect.addAttribute("sort", query.sort());
        }
    }
}
