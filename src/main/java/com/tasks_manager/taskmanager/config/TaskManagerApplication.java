package com.tasks_manager.taskmanager.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = {
        "com.tasks_manager.taskmanager.entities"
})
@EnableJpaRepositories(basePackages = {
        "com.tasks_manager.taskmanager.repositories"
})
@ComponentScan(basePackages = {
        "com.tasks_manager.taskmanager",
        "com.tasks_manager.taskmanager.config",
        "com.tasks_manager.taskmanager.controllers",
        "com.tasks_manager.taskmanager.services",
        "com.tasks_manager.taskmanager.entities",
        "com.tasks_manager.taskmanager.repositories",
        "com.tasks_manager.taskmanager.dto"
})
public class TaskManagerApplication {
    public static void main(String[] args) {
        SpringApplication.run(TaskManagerApplication.class, args);
    }
}