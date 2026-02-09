package com.tasks_manager.taskmanager.web;

import com.tasks_manager.taskmanager.dto.TaskFilter;
import com.tasks_manager.taskmanager.dto.TaskQuery;
import com.tasks_manager.taskmanager.dto.TaskSort;
import com.tasks_manager.taskmanager.entities.Priority;
import java.util.HashMap;
import java.util.Map;
import org.springframework.ui.Model;
import org.springframework.web.util.UriComponentsBuilder;

public final class TaskLinks {

    private TaskLinks() {
    }

    public static void addTo(Model model, TaskQuery query) {
        model.addAttribute("filterLinks", filterLinks(query));
        model.addAttribute("priorityLinks", priorityLinks(query));
    }

    static Map<String, String> filterLinks(TaskQuery query) {
        var links = new HashMap<String, String>();
        for (var filter : TaskFilter.values()) {
            links.put(filter.name(), build(new TaskQuery(filter, query.priority(), query.q(), query.sort())));
        }
        return links;
    }

    static Map<String, String> priorityLinks(TaskQuery query) {
        var links = new HashMap<String, String>();
        for (var priority : Priority.values()) {
            var next = priority == query.priority() ? null : priority;
            links.put(priority.name(), build(new TaskQuery(query.filter(), next, query.q(), query.sort())));
        }
        return links;
    }

    static String build(TaskQuery query) {
        var uri = UriComponentsBuilder.fromPath("/");
        if (query.filter() != TaskFilter.ALL) {
            uri.queryParam("filter", query.filter());
        }
        if (query.priority() != null) {
            uri.queryParam("priority", query.priority());
        }
        if (query.q() != null) {
            uri.queryParam("q", query.q());
        }
        if (query.sort() != TaskSort.NEWEST) {
            uri.queryParam("sort", query.sort());
        }
        return uri.encode().build().toUriString();
    }
}
