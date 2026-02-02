package com.tasks_manager.taskmanager.controllers;

import com.tasks_manager.taskmanager.dto.TaskForm;
import com.tasks_manager.taskmanager.dto.TaskQuery;
import com.tasks_manager.taskmanager.dto.TaskResponse;
import com.tasks_manager.taskmanager.dto.TaskStats;
import com.tasks_manager.taskmanager.security.AppUserPrincipal;
import com.tasks_manager.taskmanager.services.TaskService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/tasks")
public class TaskApiController {

    private final TaskService taskService;

    public TaskApiController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public List<TaskResponse> list(@AuthenticationPrincipal AppUserPrincipal user, TaskQuery query) {
        return taskService.findTasks(user.id(), query);
    }

    @GetMapping("/stats")
    public TaskStats stats(@AuthenticationPrincipal AppUserPrincipal user) {
        return taskService.stats(user.id());
    }

    @GetMapping("/{id}")
    public TaskResponse get(@AuthenticationPrincipal AppUserPrincipal user, @PathVariable Long id) {
        return taskService.getTask(user.id(), id);
    }

    @PostMapping
    public ResponseEntity<TaskResponse> create(@AuthenticationPrincipal AppUserPrincipal user,
                                               @Valid @RequestBody TaskForm form) {
        var task = taskService.createTask(user.id(), form);
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(task.id())
                .toUri();
        return ResponseEntity.created(location).body(task);
    }

    @PutMapping("/{id}")
    public TaskResponse update(@AuthenticationPrincipal AppUserPrincipal user, @PathVariable Long id,
                               @Valid @RequestBody TaskForm form) {
        return taskService.updateTask(user.id(), id, form);
    }

    @PatchMapping("/{id}/complete")
    public TaskResponse complete(@AuthenticationPrincipal AppUserPrincipal user, @PathVariable Long id) {
        return taskService.completeTask(user.id(), id);
    }

    @PatchMapping("/{id}/reopen")
    public TaskResponse reopen(@AuthenticationPrincipal AppUserPrincipal user, @PathVariable Long id) {
        return taskService.reopenTask(user.id(), id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal AppUserPrincipal user, @PathVariable Long id) {
        taskService.deleteTask(user.id(), id);
    }
}
