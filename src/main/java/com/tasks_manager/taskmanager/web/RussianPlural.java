package com.tasks_manager.taskmanager.web;

import org.springframework.stereotype.Component;

@Component("plural")
public class RussianPlural {

    public String tasks(long count) {
        return of(count, "задача", "задачи", "задач");
    }

    static String of(long count, String one, String few, String many) {
        var lastTwo = Math.abs(count) % 100;
        var last = lastTwo % 10;
        if (lastTwo >= 11 && lastTwo <= 14) {
            return many;
        }
        if (last == 1) {
            return one;
        }
        return last >= 2 && last <= 4 ? few : many;
    }
}
