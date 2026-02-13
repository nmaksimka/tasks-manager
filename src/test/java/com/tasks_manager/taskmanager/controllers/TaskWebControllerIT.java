package com.tasks_manager.taskmanager.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tasks_manager.taskmanager.IntegrationTest;
import com.tasks_manager.taskmanager.dto.TaskForm;
import com.tasks_manager.taskmanager.entities.Priority;
import com.tasks_manager.taskmanager.security.AppUserPrincipal;
import com.tasks_manager.taskmanager.services.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
class TaskWebControllerIT extends IntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private TaskService taskService;

    @Autowired
    private UserDetailsService userDetailsService;

    private Long aliceId;
    private AppUserPrincipal alice;
    private AppUserPrincipal bob;

    @BeforeEach
    void users() {
        aliceId = register("alice");
        register("bob");
        alice = (AppUserPrincipal) userDetailsService.loadUserByUsername("alice");
        bob = (AppUserPrincipal) userDetailsService.loadUserByUsername("bob");
    }

    @Test
    void anonymousIsRedirectedToLogin() throws Exception {
        mvc.perform(get("/")).andExpect(status().is3xxRedirection()).andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void loginAndRegisterPagesAreOpen() throws Exception {
        mvc.perform(get("/login")).andExpect(status().isOk()).andExpect(content().string(containsString("Вход")));
        mvc.perform(get("/register")).andExpect(status().isOk());
    }

    @Test
    void indexShowsOnlyOwnTasks() throws Exception {
        taskService.createTask(aliceId, new TaskForm("Задача Алисы", null, Priority.HIGH, TODAY));

        mvc.perform(get("/").with(user(alice)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Задача Алисы")));
        mvc.perform(get("/").with(user(bob)))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("Задача Алисы"))));
    }

    @Test
    void postWithoutCsrfIsRejected() throws Exception {
        mvc.perform(post("/tasks").with(user(alice)).param("title", "Без токена").param("priority", "LOW"))
                .andExpect(status().isForbidden());

        assertThat(taskRepository.count()).isZero();
    }

    @Test
    void createTaskFromForm() throws Exception {
        mvc.perform(post("/tasks").with(user(alice)).with(csrf())
                        .param("title", "Новая")
                        .param("priority", "HIGH")
                        .param("deadline", "2026-03-12"))
                .andExpect(redirectedUrl("/"));

        var task = taskRepository.findAll().getFirst();
        assertThat(task.getTitle()).isEqualTo("Новая");
        assertThat(task.getPriority()).isEqualTo(Priority.HIGH);
        assertThat(task.getDeadline()).isEqualTo(TODAY.plusDays(2));
    }

    @Test
    void invalidFormIsShownAgainWithErrors() throws Exception {
        mvc.perform(post("/tasks").with(user(alice)).with(csrf()).param("title", " ").param("priority", "LOW"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("form", "title"))
                .andExpect(content().string(containsString("Введите название задачи")));

        assertThat(taskRepository.count()).isZero();
    }

    @Test
    void foreignTaskPagesReturnNotFound() throws Exception {
        var task = taskService.createTask(aliceId, new TaskForm("Секрет", null, Priority.LOW, null));

        mvc.perform(get("/tasks/{id}/edit", task.id()).with(user(bob))).andExpect(status().isNotFound());
        mvc.perform(post("/tasks/{id}/delete", task.id()).with(user(bob)).with(csrf()))
                .andExpect(status().isNotFound());

        assertThat(taskRepository.existsById(task.id())).isTrue();
    }

    @Test
    void toggleKeepsCurrentFilter() throws Exception {
        var task = taskService.createTask(aliceId, new TaskForm("Задача", null, Priority.LOW, null));

        mvc.perform(post("/tasks/{id}/toggle", task.id()).with(user(alice)).with(csrf())
                        .param("filter", "ACTIVE")
                        .param("q", "зад"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/?filter=ACTIVE&q=*"));

        assertThat(taskService.getTask(aliceId, task.id()).completed()).isTrue();
    }

    @Test
    void registrationCreatesAccount() throws Exception {
        mvc.perform(post("/register").with(csrf())
                        .param("username", "newbie")
                        .param("email", "newbie@example.com")
                        .param("password", "password123"))
                .andExpect(redirectedUrl("/login?registered"));

        assertThat(userRepository.findByUsername("newbie")).isPresent();
    }

    @Test
    void duplicateRegistrationShowsFieldError() throws Exception {
        mvc.perform(post("/register").with(csrf())
                        .param("username", "ALICE")
                        .param("email", "other@example.com")
                        .param("password", "password123"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("form", "username"));
    }

    @Test
    void profileUpdateChangesName() throws Exception {
        mvc.perform(post("/profile").with(user(alice)).with(csrf()).param("firstName", "Алиса"))
                .andExpect(redirectedUrl("/profile"));

        assertThat(userService.getUser(aliceId).getDisplayName()).isEqualTo("Алиса");
    }
}
