package com.yandex.app.model;

import com.yandex.app.enums.Status;
import com.yandex.app.enums.Type;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Objects;

public class Task {
    private String name;
    private String description;
    private int id;
    private Status status;
    private Duration duration;
    private ZonedDateTime startTime;

    public Task(int id, String name, String description, Status status) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.status = status;
        this.duration = Duration.ofSeconds(0);
    }

    public Task(String name, String description, Status status) {
        this.name = name;
        this.description = description;
        this.status = status;
        this.duration = Duration.ofSeconds(0);
    }

    public Task(String name, String description) {
        this.name = name;
        this.description = description;
        this.status = Status.NEW;
        this.duration = Duration.ofSeconds(0);
    }

    public Task(String name, String description, Status status, Duration duration, ZonedDateTime startTime) {
        this(name, description, status);
        this.duration = duration;
        this.startTime = startTime;
    }

    public Task(int id, String name, String description, Status status, Duration duration, ZonedDateTime startTime) {
        this(id, name, description, status);
        this.duration = duration;
        this.startTime = startTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return Objects.equals(name, task.name) && Objects.equals(description, task.description) && Objects.equals(id, task.id) && status == task.status;
    }

    @Override
    public String toString() {
        return "com.yandex.app.model.Task{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", id=" + id +
                ", status=" + status +
                ", duration=" + duration +
                ", startTime=" + startTime +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, description, id, status);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Type getType() {
        return Type.TASK;
    }

    public ZonedDateTime getEndTime() {
        return startTime.plus(duration);
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public ZonedDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(ZonedDateTime startTime) {
        this.startTime = startTime;
    }
}
