package com.tasks_manager.taskmanager.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.tasks_manager.taskmanager.entities.Priority;
import org.junit.jupiter.api.Test;

class TaskQueryTest {

    @Test
    void nullValuesBecomeDefaults() {
        var query = new TaskQuery(null, null, null, null);

        assertThat(query.filter()).isEqualTo(TaskFilter.ALL);
        assertThat(query.sort()).isEqualTo(TaskSort.NEWEST);
        assertThat(query.priority()).isNull();
        assertThat(query.q()).isNull();
    }

    @Test
    void blankSearchIsIgnoredAndTextIsStripped() {
        assertThat(new TaskQuery(null, null, "   ", null).q()).isNull();
        assertThat(new TaskQuery(null, null, "  отчёт ", null).q()).isEqualTo("отчёт");
    }

    @Test
    void statsCountMatchesFilter() {
        var stats = new TaskStats(10, 7, 3, 2, 4, 1);

        assertThat(stats.count(TaskFilter.ALL)).isEqualTo(10);
        assertThat(stats.count(TaskFilter.ACTIVE)).isEqualTo(7);
        assertThat(stats.count(TaskFilter.COMPLETED)).isEqualTo(3);
        assertThat(stats.count(TaskFilter.TODAY)).isEqualTo(2);
        assertThat(stats.count(TaskFilter.WEEK)).isEqualTo(4);
        assertThat(stats.count(TaskFilter.OVERDUE)).isEqualTo(1);
        assertThat(stats.completionPercent()).isEqualTo(30);
    }

    @Test
    void emptyStatsHaveZeroPercent() {
        assertThat(new TaskStats(0, 0, 0, 0, 0, 0).completionPercent()).isZero();
    }

    @Test
    void priorityRoundTripsThroughCode() {
        for (var priority : Priority.values()) {
            assertThat(Priority.fromCode(priority.getCode())).isEqualTo(priority);
        }
    }
}
