package com.tasks_manager.taskmanager.controllers;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tasks_manager.taskmanager.IntegrationTest;
import com.tasks_manager.taskmanager.dto.TaskForm;
import com.tasks_manager.taskmanager.entities.Priority;
import com.tasks_manager.taskmanager.services.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
class TaskApiControllerIT extends IntegrationTest {

    private static final String PASSWORD = "password123";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private TaskService taskService;

    private Long aliceId;

    @BeforeEach
    void users() {
        aliceId = register("alice");
        register("bob");
    }

    @Test
    void requestWithoutCredentialsGets401() throws Exception {
        mvc.perform(get("/api/tasks")).andExpect(status().isUnauthorized());
    }

    @Test
    void wrongPasswordGets401() throws Exception {
        mvc.perform(get("/api/tasks").with(httpBasic("alice", "wrong"))).andExpect(status().isUnauthorized());
    }

    @Test
    void createReturnsLocationAndDtoWithoutUser() throws Exception {
        mvc.perform(post("/api/tasks").with(httpBasic("alice", PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "API задача", "priority": "HIGH", "deadline": "2026-03-09"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/tasks/")))
                .andExpect(jsonPath("$.title").value("API задача"))
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andExpect(jsonPath("$.deadlineStatus").value("OVERDUE"))
                .andExpect(jsonPath("$.user").doesNotExist())
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void invalidBodyReturnsProblemWithFieldErrors() throws Exception {
        mvc.perform(post("/api/tasks").with(httpBasic("alice", PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "", "priority": null}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.title").exists())
                .andExpect(jsonPath("$.errors.priority").exists());
    }

    @Test
    void foreignTaskReturns404() throws Exception {
        var task = taskService.createTask(aliceId, new TaskForm("Секрет", null, Priority.LOW, null));

        mvc.perform(get("/api/tasks/{id}", task.id()).with(httpBasic("bob", PASSWORD)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
        mvc.perform(delete("/api/tasks/{id}", task.id()).with(httpBasic("bob", PASSWORD)))
                .andExpect(status().isNotFound());
    }

    @Test
    void fullLifecycle() throws Exception {
        var task = taskService.createTask(aliceId, new TaskForm("Задача", null, Priority.LOW, null));

        mvc.perform(put("/api/tasks/{id}", task.id()).with(httpBasic("alice", PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "Обновлена", "priority": "MEDIUM"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Обновлена"));
        mvc.perform(patch("/api/tasks/{id}/complete", task.id()).with(httpBasic("alice", PASSWORD)))
                .andExpect(jsonPath("$.completed").value(true));
        mvc.perform(get("/api/tasks").param("filter", "COMPLETED").with(httpBasic("alice", PASSWORD)))
                .andExpect(jsonPath("$", hasSize(1)));
        mvc.perform(patch("/api/tasks/{id}/reopen", task.id()).with(httpBasic("alice", PASSWORD)))
                .andExpect(jsonPath("$.completed").value(false));
        mvc.perform(get("/api/tasks/stats").with(httpBasic("alice", PASSWORD)))
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.active").value(1));
        mvc.perform(delete("/api/tasks/{id}", task.id()).with(httpBasic("alice", PASSWORD)))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/tasks").with(httpBasic("alice", PASSWORD))).andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void apiDoesNotNeedCsrfAndDoesNotCreateSession() throws Exception {
        mvc.perform(post("/api/tasks").with(httpBasic("alice", PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "Без сессии", "priority": "LOW"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().doesNotExist("Set-Cookie"));
    }
}
