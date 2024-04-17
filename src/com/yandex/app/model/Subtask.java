package com.yandex.app.model;

import com.yandex.app.enums.Status;
import com.yandex.app.enums.Type;

import java.time.Duration;
import java.time.ZonedDateTime;

public class Subtask extends Task {

    private int epicId;

    public Subtask(int id, String name, String description, int epicId, Status status) {
        super(id, name, description, status);
        this.epicId = epicId;
    }

    public Subtask(String name, String description, int epicId, Status status) {
        super(name, description, status);
        this.epicId = epicId;
    }

    public Subtask(String name, String description, int epicId) {
        super(name, description);
        this.epicId = epicId;
    }

    public Subtask(String name, String description, int epicId, Status status, Duration duration, ZonedDateTime startTime) {
        super(name, description, status, duration, startTime);
        this.epicId = epicId;
    }

    public Subtask(int id, String name, String description, int epicId, Status status, Duration duration, ZonedDateTime startTime) {
        super(id, name, description, status, duration, startTime);
        this.epicId = epicId;
    }

    @Override
    public String toString() {
        return "com.yandex.app.model.Subtask{" +
                "name='" + getName() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", id=" + getId() +
                ", epicId=" + epicId +
                ", status=" + getStatus() +
                ", duration=" + getDuration() +
                ", startTime=" + getStartTime() +
                '}';
    }

    public int getEpicId() {
        return epicId;
    }

    public void setEpicId(int epicId) {
        this.epicId = epicId;
    }

    @Override
    public Type getType() {
        return Type.SUBTASK;
    }
}
