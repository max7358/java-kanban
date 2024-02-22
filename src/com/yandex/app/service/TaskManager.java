package com.yandex.app.service;

import com.yandex.app.enums.Status;
import com.yandex.app.model.Epic;
import com.yandex.app.model.Subtask;
import com.yandex.app.model.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TaskManager {
    final private HashMap<Integer, Task> tasks;
    final private HashMap<Integer, Subtask> subtasks;
    final private HashMap<Integer, Epic> epics;

    public TaskManager() {
        tasks = new HashMap<>();
        subtasks = new HashMap<>();
        epics = new HashMap<>();
    }

    //com.yandex.app.model.Task methods
    //get all tasks
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    //delete all tasks
    public void deleteAllTasks() {
        tasks.clear();
    }

    //add task
    public Task addTask(Task task) {
        task.setId(IdGenerator.generateId());
        tasks.put(task.getId(), task);
        return task;
    }

    //update task
    public Task updateTask(Task task) {
        tasks.put(task.getId(), task);
        return task;
    }

    //get task by id
    public Task getTaskById(int id) {
        return tasks.get(id);
    }

    //delete task by id
    public Task deleteTaskById(int id) {
        return tasks.remove(id);
    }

    //subtask methods
    //add subtask, assume it can't exist w/o epic
    public void addSubtask(Subtask subtask) {
        subtask.setId(IdGenerator.generateId());
        epics.get(subtask.getEpicId()).addSubtaskId(subtask.getId());
        subtasks.put(subtask.getId(), subtask);
    }

    //get all subtasks
    public List<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    //get subtask by id
    public Task getSubtaskById(int id) {
        return subtasks.get(id);
    }

    //delete all subtasks, remove epic id link, update epic status
    public void deleteAllSubtasks() {
        subtasks.values().forEach(s -> {
            Epic epic = epics.get(s.getEpicId());
            if (epic != null) {
                epic.removeSubtaskId(s.getId());
                updateEpicStatus(epics.get(s.getEpicId()));
            }
        });
        subtasks.clear();
    }

    //delete subtask by id, remove epic id link, update epic status
    public void deleteSubtaskById(int id) {
        int epicId = subtasks.get(id).getEpicId();
        epics.get(epicId).removeSubtaskId(id);
        updateEpicStatus(epics.get(epicId));
        subtasks.remove(id);
    }

    //update subtask, update epic status
    public Subtask updateSubtask(Subtask subtask) {
        subtasks.put(subtask.getId(), subtask);
        updateEpicStatus(epics.get(subtask.getEpicId()));
        return subtask;
    }

    //epic methods
    //add epic
    public void addEpic(Epic epic) {
        epic.setId(IdGenerator.generateId());
        updateEpicStatus(epic);
        epics.put(epic.getId(), epic);
    }

    //get epic by id
    public Task getEpicById(int id) {
        return epics.get(id);
    }

    //get epics subtasks
    public ArrayList<Subtask> getEpicSubtasks(int id) {
        ArrayList<Subtask> epicSubtasks = new ArrayList<>();
        epics.get(id).getSubtaskIds().forEach(subId -> epicSubtasks.add(subtasks.get(subId)));
        return epicSubtasks;
    }

    //changes epic status, depending on subtasks status
    private void updateEpicStatus(Epic epic) {
        if (epic.getSubtaskIds().isEmpty()) {
            epic.setStatus(Status.NEW);
        } else {
            Status status = null;
            for (Integer s : epic.getSubtaskIds()) {
                switch (subtasks.get(s).getStatus()) {
                    case NEW:
                        if (status == null) {
                            status = Status.NEW;
                        } else if (status == Status.NEW) {
                            break;
                        } else {
                            status = Status.IN_PROGRESS;
                        }
                        break;
                    case DONE:
                        if (status == null) {
                            status = Status.DONE;
                        } else if (status == Status.DONE) {
                            break;
                        } else {
                            status = Status.IN_PROGRESS;
                        }
                        break;
                    case IN_PROGRESS:
                        status = Status.IN_PROGRESS;
                        break;
                    default:
                        break;
                }
            }
            epic.setStatus(status);
        }
    }

    //get all epics
    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    //delete all epics and all linked subtasks
    public void deleteAllEpics() {
        epics.values().forEach(epic -> deleteSubtasksByIds(epic.getSubtaskIds()));
        epics.clear();
    }

    //delete epic by id and linked subtask
    public void deleteEpicById(int id) {
        deleteSubtasksByIds(epics.get(id).getSubtaskIds());
        epics.remove(id);
    }

    //delete subtasks by id
    //private method only for "delete com.yandex.app.model.Epic" cases
    private void deleteSubtasksByIds(ArrayList<Integer> subtaskIds) {
        subtaskIds.forEach(subtasks::remove);
    }

    //update epic
    public Epic updateEpic(Epic epic) {
        epics.put(epic.getId(), epic);
        return epic;
    }

}
