package com.tasks_manager.taskmanager.dto;

import org.springframework.data.domain.Sort;

public enum TaskSort {
    NEWEST("Сначала новые", Sort.by(Sort.Order.desc("createdAt"))),
    DEADLINE("По сроку", Sort.by(Sort.Order.asc("deadline").nullsLast(), Sort.Order.desc("createdAt"))),
    PRIORITY("По приоритету", Sort.by(Sort.Order.asc("priority"), Sort.Order.asc("deadline").nullsLast())),
    TITLE("По названию", Sort.by(Sort.Order.asc("title").ignoreCase()));

    private final String title;
    private final Sort sort;

    TaskSort(String title, Sort sort) {
        this.title = title;
        this.sort = sort;
    }

    public String getTitle() {
        return title;
    }

    public Sort toSort() {
        return sort;
    }
}
