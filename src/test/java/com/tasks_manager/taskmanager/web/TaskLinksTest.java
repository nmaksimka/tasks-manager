package com.tasks_manager.taskmanager.web;

import static org.assertj.core.api.Assertions.assertThat;

import com.tasks_manager.taskmanager.dto.TaskFilter;
import com.tasks_manager.taskmanager.dto.TaskQuery;
import com.tasks_manager.taskmanager.dto.TaskSort;
import com.tasks_manager.taskmanager.entities.Priority;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class TaskLinksTest {

    @Test
    void defaultQueryLinksToRoot() {
        assertThat(TaskLinks.build(TaskQuery.all())).isEqualTo("/");
    }

    @Test
    void filterLinkKeepsOtherParameters() {
        var query = new TaskQuery(TaskFilter.ACTIVE, Priority.HIGH, "отчёт", TaskSort.DEADLINE);

        var link = TaskLinks.filterLinks(query).get("OVERDUE");

        assertThat(link).startsWith("/?filter=OVERDUE&priority=HIGH&q=")
                .endsWith("&sort=DEADLINE")
                .doesNotContain("отчёт");
    }

    @Test
    void clickingActivePriorityRemovesIt() {
        var query = new TaskQuery(TaskFilter.ALL, Priority.LOW, null, null);

        assertThat(TaskLinks.priorityLinks(query).get("LOW")).isEqualTo("/");
        assertThat(TaskLinks.priorityLinks(query).get("HIGH")).isEqualTo("/?priority=HIGH");
    }

    @ParameterizedTest
    @CsvSource({
            "0, задач",
            "1, задача",
            "2, задачи",
            "4, задачи",
            "5, задач",
            "11, задач",
            "14, задач",
            "21, задача",
            "22, задачи",
            "111, задач"
    })
    void russianPlural(long count, String expected) {
        assertThat(new RussianPlural().tasks(count)).isEqualTo(expected);
    }
}
