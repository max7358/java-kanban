package com.yandex.app.model;

import com.yandex.app.enums.Status;
import com.yandex.app.enums.Type;

import java.util.ArrayList;

public class Epic extends Task {
    private final ArrayList<Integer> subtaskIds = new ArrayList<>();

    public Epic(int id, String name, String description, Status status) {
        super(id, name, description, status);
    }

    public Epic(String name, String description, Status status) {
        super(name, description, status);
    }

    public Epic(String name, String description) {
        super(name, description);
    }

    @Override
    public String toString() {
        return "com.yandex.app.model.Epic{" +
                "name='" + getName() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", id=" + getId() +
                ", subtaskIds=" + subtaskIds +
                ", status=" + getStatus() +
                '}';
    }

    public ArrayList<Integer> getSubtaskIds() {
        return subtaskIds;
    }

    public void addSubtaskId(int id) {
        subtaskIds.add(id);
    }

    public void removeSubtaskId(Integer id) {
        subtaskIds.remove(id);
    }

    @Override
    public Type getType() {
        return Type.EPIC;
    }
}
