package com.tasks_manager.taskmanager.entities;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class PriorityConverter implements AttributeConverter<Priority, Integer> {

    @Override
    public Integer convertToDatabaseColumn(Priority priority) {
        return priority == null ? null : priority.getCode();
    }

    @Override
    public Priority convertToEntityAttribute(Integer code) {
        return code == null ? null : Priority.fromCode(code);
    }
}
