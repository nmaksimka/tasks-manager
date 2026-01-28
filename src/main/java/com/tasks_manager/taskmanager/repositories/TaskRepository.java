package com.tasks_manager.taskmanager.repositories;

import com.tasks_manager.taskmanager.entities.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByUserId(Long userId);
    List<Task> findByUserIdAndCompletedFalse(Long userId);
    List<Task> findByUserIdAndCompletedTrue(Long userId);
    List<Task> findByUserIdAndPriority(Long userId, Integer priority);

    @Query("SELECT t FROM Task t WHERE " +
            "t.user.id = :userId AND " +
            "(LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Task> searchByUser(@Param("userId") Long userId, @Param("keyword") String keyword);

    List<Task> findByCompleted(boolean completed);
    List<Task> findByPriority(Integer priority);

    List<Task> findByUserIdAndDeadlineBeforeAndCompletedFalse(Long userId, LocalDate date);
    List<Task> findByUserIdAndDeadlineAndCompletedFalse(Long userId, LocalDate date);
    List<Task> findByUserIdAndDeadlineBetweenAndCompletedFalse(Long userId, LocalDate start, LocalDate end);

    List<Task> findByDeadlineBeforeAndCompletedFalse(LocalDate date);
    List<Task> findByDeadlineAndCompletedFalse(LocalDate date);
    List<Task> findByDeadlineBetweenAndCompletedFalse(LocalDate start, LocalDate end);
}
