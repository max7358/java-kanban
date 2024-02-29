package com.yandex.app.service;

import com.yandex.app.enums.Status;
import com.yandex.app.model.Epic;
import com.yandex.app.model.Subtask;
import com.yandex.app.model.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InMemoryTaskManager implements TaskManager {
    final private HashMap<Integer, Task> tasks;
    final private HashMap<Integer, Subtask> subtasks;
    final private HashMap<Integer, Epic> epics;
    HistoryManager historyManager;

    public InMemoryTaskManager(HistoryManager historyManager) {
        tasks = new HashMap<>();
        subtasks = new HashMap<>();
        epics = new HashMap<>();
        this.historyManager = historyManager;
    }

    //Task methods
    //get all tasks
    @Override
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    //delete all tasks
    @Override
    public void deleteAllTasks() {
        tasks.clear();
    }

    //add task
    @Override
    public Task addTask(Task task) {
        task.setId(idGenerator.generateId());
        tasks.put(task.getId(), task);
        return task;
    }

    //update task
    @Override
    public Task updateTask(Task task) {
        tasks.put(task.getId(), task);
        return task;
    }

    //get task by id
    @Override
    public Task getTaskById(int id) {
        historyManager.add(tasks.get(id));
        return tasks.get(id);
    }

    //delete task by id
    @Override
    public Task deleteTaskById(int id) {
        return tasks.remove(id);
    }

    //subtask methods
    //add subtask, assume it can't exist w/o epic
    @Override
    public void addSubtask(Subtask subtask) {
        subtask.setId(idGenerator.generateId());
        epics.get(subtask.getEpicId()).addSubtaskId(subtask.getId());
        subtasks.put(subtask.getId(), subtask);
    }

    //get all subtasks
    @Override
    public List<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    //get subtask by id
    @Override
    public Task getSubtaskById(int id) {
        historyManager.add(subtasks.get(id));
        return subtasks.get(id);
    }

    //delete all subtasks, remove epic id link, update epic status
    @Override
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
    @Override
    public void deleteSubtaskById(int id) {
        int epicId = subtasks.get(id).getEpicId();
        epics.get(epicId).removeSubtaskId(id);
        updateEpicStatus(epics.get(epicId));
        subtasks.remove(id);
    }

    //update subtask, update epic status
    @Override
    public Subtask updateSubtask(Subtask subtask) {
        subtasks.put(subtask.getId(), subtask);
        updateEpicStatus(epics.get(subtask.getEpicId()));
        return subtask;
    }

    //epic methods
    //add epic
    @Override
    public void addEpic(Epic epic) {
        epic.setId(idGenerator.generateId());
        updateEpicStatus(epic);
        epics.put(epic.getId(), epic);
    }

    //get epic by id
    @Override
    public Task getEpicById(int id) {
        historyManager.add(epics.get(id));
        return epics.get(id);
    }

    //get epics subtasks
    @Override
    public ArrayList<Subtask> getEpicSubtasksById(int id) {
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
    @Override
    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    //delete all epics and all linked subtasks
    @Override
    public void deleteAllEpics() {
        epics.values().forEach(epic -> deleteSubtasksByIds(epic.getSubtaskIds()));
        epics.clear();
    }

    //delete epic by id and linked subtask
    @Override
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
    @Override
    public Epic updateEpic(Epic epic) {
        epics.put(epic.getId(), epic);
        return epic;
    }

    public List<Task> getHistory() {
        return historyManager.getHistory();
    }
}
