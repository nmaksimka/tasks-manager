package com.tasks_manager.taskmanager.repositories;

import com.tasks_manager.taskmanager.entities.Task;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {

    Optional<Task> findByIdAndUserId(Long id, Long userId);

    long countByUserId(Long userId);

    long countByUserIdAndCompletedTrue(Long userId);

    long countByUserIdAndCompletedFalseAndDeadline(Long userId, LocalDate deadline);

    long countByUserIdAndCompletedFalseAndDeadlineBefore(Long userId, LocalDate deadline);
}
