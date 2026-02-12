package com.tasks_manager.taskmanager.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tasks_manager.taskmanager.IntegrationTest;
import com.tasks_manager.taskmanager.dto.DeadlineStatus;
import com.tasks_manager.taskmanager.dto.TaskFilter;
import com.tasks_manager.taskmanager.dto.TaskForm;
import com.tasks_manager.taskmanager.dto.TaskQuery;
import com.tasks_manager.taskmanager.dto.TaskResponse;
import com.tasks_manager.taskmanager.dto.TaskSort;
import com.tasks_manager.taskmanager.entities.Priority;
import com.tasks_manager.taskmanager.exceptions.TaskNotFoundException;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class TaskServiceIT extends IntegrationTest {

    @Autowired
    private TaskService taskService;

    private Long alice;
    private Long bob;

    @BeforeEach
    void users() {
        alice = register("alice");
        bob = register("bob");
    }

    @Test
    void createdTaskBelongsToAuthorAndIsActive() {
        var task = create(alice, "  Отчёт  ", Priority.HIGH, TODAY);

        assertThat(task.title()).isEqualTo("Отчёт");
        assertThat(task.completed()).isFalse();
        assertThat(task.deadlineStatus()).isEqualTo(DeadlineStatus.TODAY);
        assertThat(task.createdAt()).isEqualTo(TODAY.atTime(12, 0));
        assertThat(taskService.findTasks(alice, TaskQuery.all())).extracting(TaskResponse::id).containsExactly(task.id());
        assertThat(taskService.findTasks(bob, TaskQuery.all())).isEmpty();
    }

    @Test
    void foreignTaskLooksLikeMissingTask() {
        var task = create(alice, "Секрет", Priority.LOW, null);
        var form = new TaskForm("Взлом", null, Priority.LOW, null);

        assertThatThrownBy(() -> taskService.getTask(bob, task.id())).isInstanceOf(TaskNotFoundException.class);
        assertThatThrownBy(() -> taskService.updateTask(bob, task.id(), form)).isInstanceOf(TaskNotFoundException.class);
        assertThatThrownBy(() -> taskService.toggleTask(bob, task.id())).isInstanceOf(TaskNotFoundException.class);
        assertThatThrownBy(() -> taskService.deleteTask(bob, task.id())).isInstanceOf(TaskNotFoundException.class);
        assertThat(taskService.getTask(alice, task.id()).title()).isEqualTo("Секрет");
    }

    @Test
    void updateCanClearDescriptionAndDeadline() {
        var task = taskService.createTask(alice, new TaskForm("Задача", "Описание", Priority.MEDIUM, TODAY));

        var updated = taskService.updateTask(alice, task.id(), new TaskForm("Задача", "   ", Priority.LOW, null));

        assertThat(updated.description()).isNull();
        assertThat(updated.deadline()).isNull();
        assertThat(updated.priority()).isEqualTo(Priority.LOW);
    }

    @Test
    void toggleCompletesAndReopens() {
        var task = create(alice, "Тренировка", Priority.MEDIUM, null);

        var done = taskService.toggleTask(alice, task.id());
        assertThat(done.completed()).isTrue();
        assertThat(done.completedAt()).isEqualTo(TODAY.atTime(12, 0));

        var reopened = taskService.toggleTask(alice, task.id());
        assertThat(reopened.completed()).isFalse();
        assertThat(reopened.completedAt()).isNull();
    }

    @Test
    void completeIsIdempotent() {
        var task = create(alice, "Задача", Priority.MEDIUM, null);

        taskService.completeTask(alice, task.id());
        var again = taskService.completeTask(alice, task.id());

        assertThat(again.completed()).isTrue();
    }

    @Test
    void filtersSplitTasksByDeadline() {
        var overdue = create(alice, "Просрочена", Priority.MEDIUM, TODAY.minusDays(1));
        var today = create(alice, "Сегодня", Priority.MEDIUM, TODAY);
        var week = create(alice, "Через 5 дней", Priority.MEDIUM, TODAY.plusDays(5));
        var later = create(alice, "Через месяц", Priority.MEDIUM, TODAY.plusMonths(1));
        var done = create(alice, "Сделана", Priority.MEDIUM, TODAY);
        taskService.completeTask(alice, done.id());

        assertThat(titles(TaskFilter.OVERDUE)).containsExactly(overdue.title());
        assertThat(titles(TaskFilter.TODAY)).containsExactly(today.title());
        assertThat(titles(TaskFilter.WEEK)).containsExactlyInAnyOrder(today.title(), week.title());
        assertThat(titles(TaskFilter.ACTIVE)).hasSize(4).doesNotContain(done.title());
        assertThat(titles(TaskFilter.COMPLETED)).containsExactly(done.title());
        assertThat(titles(TaskFilter.ALL)).hasSize(5).contains(later.title());
    }

    @Test
    void statsMatchFilters() {
        create(alice, "Просрочена", Priority.MEDIUM, TODAY.minusDays(2));
        create(alice, "Сегодня", Priority.MEDIUM, TODAY);
        var done = create(alice, "Сделана", Priority.MEDIUM, null);
        taskService.completeTask(alice, done.id());
        create(bob, "Чужая", Priority.MEDIUM, TODAY.minusDays(2));

        var stats = taskService.stats(alice);

        assertThat(stats.total()).isEqualTo(3);
        assertThat(stats.active()).isEqualTo(2);
        assertThat(stats.completed()).isEqualTo(1);
        assertThat(stats.today()).isEqualTo(1);
        assertThat(stats.week()).isEqualTo(1);
        assertThat(stats.overdue()).isEqualTo(1);
    }

    @Test
    void searchMatchesTitleAndDescriptionIgnoringCase() {
        create(alice, "Купить МОЛОКО", Priority.LOW, null);
        taskService.createTask(alice, new TaskForm("Магазин", "хлеб и молоко", Priority.LOW, null));
        create(alice, "Тренировка", Priority.LOW, null);

        var found = taskService.findTasks(alice, new TaskQuery(null, null, "молоко", null));

        assertThat(found).extracting(TaskResponse::title).containsExactlyInAnyOrder("Купить МОЛОКО", "Магазин");
    }

    @Test
    void searchTreatsWildcardsLiterally() {
        create(alice, "Скидка 50% на всё", Priority.LOW, null);
        create(alice, "Скидка 500 рублей", Priority.LOW, null);
        create(alice, "snake_case", Priority.LOW, null);
        create(alice, "snakeXcase", Priority.LOW, null);

        assertThat(search("50%")).containsExactly("Скидка 50% на всё");
        assertThat(search("_")).containsExactly("snake_case");
    }

    @Test
    void priorityFilterAndSort() {
        create(alice, "Низкий", Priority.LOW, null);
        create(alice, "Высокий", Priority.HIGH, null);
        create(alice, "Средний", Priority.MEDIUM, null);

        var sorted = taskService.findTasks(alice, new TaskQuery(null, null, null, TaskSort.PRIORITY));
        var high = taskService.findTasks(alice, new TaskQuery(null, Priority.HIGH, null, null));

        assertThat(sorted).extracting(TaskResponse::title).containsExactly("Высокий", "Средний", "Низкий");
        assertThat(high).extracting(TaskResponse::title).containsExactly("Высокий");
    }

    @Test
    void deadlineSortPutsTasksWithoutDeadlineLast() {
        create(alice, "Без срока", Priority.MEDIUM, null);
        create(alice, "Послезавтра", Priority.MEDIUM, TODAY.plusDays(2));
        create(alice, "Завтра", Priority.MEDIUM, TODAY.plusDays(1));

        var sorted = taskService.findTasks(alice, new TaskQuery(null, null, null, TaskSort.DEADLINE));

        assertThat(sorted).extracting(TaskResponse::title).containsExactly("Завтра", "Послезавтра", "Без срока");
    }

    @Test
    void deleteRemovesTask() {
        var task = create(alice, "Удалить", Priority.LOW, null);

        taskService.deleteTask(alice, task.id());

        assertThat(taskRepository.existsById(task.id())).isFalse();
    }

    private TaskResponse create(Long userId, String title, Priority priority, LocalDate deadline) {
        return taskService.createTask(userId, new TaskForm(title, null, priority, deadline));
    }

    private java.util.List<String> titles(TaskFilter filter) {
        return taskService.findTasks(alice, new TaskQuery(filter, null, null, null)).stream()
                .map(TaskResponse::title)
                .toList();
    }

    private java.util.List<String> search(String text) {
        return taskService.findTasks(alice, new TaskQuery(null, null, text, null)).stream()
                .map(TaskResponse::title)
                .toList();
    }
}
