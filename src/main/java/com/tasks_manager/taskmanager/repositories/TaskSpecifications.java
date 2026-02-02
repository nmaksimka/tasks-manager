package com.tasks_manager.taskmanager.repositories;

import com.tasks_manager.taskmanager.dto.TaskFilter;
import com.tasks_manager.taskmanager.dto.TaskQuery;
import com.tasks_manager.taskmanager.entities.Priority;
import com.tasks_manager.taskmanager.entities.Task;
import java.time.LocalDate;
import java.util.Locale;
import org.springframework.data.jpa.domain.Specification;

public final class TaskSpecifications {

    private static final char ESCAPE = '\\';

    private TaskSpecifications() {
    }

    public static Specification<Task> matching(Long userId, TaskQuery query, LocalDate today) {
        return Specification.allOf(
                ownedBy(userId),
                filter(query.filter(), today),
                priority(query.priority()),
                search(query.q())
        );
    }

    static Specification<Task> ownedBy(Long userId) {
        return (root, cq, cb) -> cb.equal(root.get("user").get("id"), userId);
    }

    static Specification<Task> filter(TaskFilter filter, LocalDate today) {
        return switch (filter) {
            case ALL -> null;
            case ACTIVE -> active();
            case COMPLETED -> (root, cq, cb) -> cb.isTrue(root.get("completed"));
            case TODAY -> active().and((root, cq, cb) -> cb.equal(root.get("deadline"), today));
            case WEEK -> active().and((root, cq, cb) -> cb.between(root.get("deadline"), today, today.plusDays(7)));
            case OVERDUE -> active().and((root, cq, cb) -> cb.lessThan(root.get("deadline"), today));
        };
    }

    static Specification<Task> priority(Priority priority) {
        return priority == null ? null : (root, cq, cb) -> cb.equal(root.get("priority"), priority);
    }

    static Specification<Task> search(String text) {
        if (text == null) {
            return null;
        }
        var pattern = "%" + escapeLike(text.toLowerCase(Locale.ROOT)) + "%";
        return (root, cq, cb) -> cb.or(
                cb.like(cb.lower(root.get("title")), pattern, ESCAPE),
                cb.like(cb.lower(root.get("description")), pattern, ESCAPE)
        );
    }

    static String escapeLike(String text) {
        return text.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }

    private static Specification<Task> active() {
        return (root, cq, cb) -> cb.isFalse(root.get("completed"));
    }
}
