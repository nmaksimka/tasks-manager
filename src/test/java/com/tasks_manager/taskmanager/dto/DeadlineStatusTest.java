package com.tasks_manager.taskmanager.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DeadlineStatusTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 3, 10);

    @ParameterizedTest
    @CsvSource({
            "-1, OVERDUE",
            "0, TODAY",
            "1, SOON",
            "3, SOON",
            "4, LATER"
    })
    void statusDependsOnDaysLeft(int daysLeft, DeadlineStatus expected) {
        assertThat(DeadlineStatus.of(TODAY.plusDays(daysLeft), false, TODAY)).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({"-5", "0", "10"})
    void completedTaskIsAlwaysDone(int daysLeft) {
        assertThat(DeadlineStatus.of(TODAY.plusDays(daysLeft), true, TODAY)).isEqualTo(DeadlineStatus.DONE);
    }

    @ParameterizedTest
    @CsvSource({"false, NONE", "true, DONE"})
    void taskWithoutDeadline(boolean completed, DeadlineStatus expected) {
        assertThat(DeadlineStatus.of(null, completed, TODAY)).isEqualTo(expected);
    }
}
