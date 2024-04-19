package com.yandex.app.service;

import com.yandex.app.enums.Status;
import com.yandex.app.exception.NotFoundException;
import com.yandex.app.exception.ValidationException;
import com.yandex.app.model.Epic;
import com.yandex.app.model.Subtask;
import com.yandex.app.model.Task;

import java.time.ZonedDateTime;
import java.util.*;

public class InMemoryTaskManager implements TaskManager {
    protected final HashMap<Integer, Task> tasks;
    protected final HashMap<Integer, Subtask> subtasks;
    protected final HashMap<Integer, Epic> epics;
    HistoryManager historyManager;
    protected int idSeq = 0;

    TreeSet<Task> prioritisedTasks = new TreeSet<>(Comparator.comparing(Task::getStartTime));

    public InMemoryTaskManager(HistoryManager historyManager) {
        tasks = new HashMap<>();
        subtasks = new HashMap<>();
        epics = new HashMap<>();
        this.historyManager = historyManager;
    }

    private int generateId() {
        return ++idSeq;
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
        removeTasksFromHistory(tasks.keySet());
        tasks.clear();
    }

    //add task
    @Override
    public Task addTask(Task task) {
        task.setId(generateId());
        if (task.getStartTime() != null) {
            prioritisedTasks.forEach(tsk -> validateTasksIntercept(tsk, task));
            prioritisedTasks.add(task);
        }
        tasks.put(task.getId(), task);
        return task;
    }

    private void validateTasksIntercept(Task existingTask, Task newTask) {
        if (newTask.getStartTime().isBefore(existingTask.getEndTime()) && newTask.getEndTime().isAfter(existingTask.getStartTime()))
            throw new ValidationException("Error: task id:" + newTask.getId() + " - " + newTask.getStartTime() + " : " + newTask.getEndTime() + "\nintercepts with existing task id:" + existingTask.getId() + " - " + existingTask.getStartTime() + " : " + existingTask.getEndTime());
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
        Task task = Optional.ofNullable(tasks.get(id)).orElseThrow(() -> new NotFoundException("Error: task id:" + id + " not found"));
        historyManager.add(task);
        return task;
    }

    //delete task by id
    @Override
    public Task deleteTaskById(int id) {
        historyManager.remove(id);
        return tasks.remove(id);
    }

    //subtask methods
    //add subtask, assume it can't exist w/o epic
    @Override
    public void addSubtask(Subtask subtask) {
        subtask.setId(generateId());
        epics.get(subtask.getEpicId()).addSubtaskId(subtask.getId());
        subtasks.put(subtask.getId(), subtask);
        updateEpicStatus(epics.get(subtask.getEpicId()));
        if (subtask.getStartTime() != null) {
            calculateEpicDuration(epics.get(subtask.getEpicId()));
        }
    }

    //get all subtasks
    @Override
    public List<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    //get subtask by id
    @Override
    public Task getSubtaskById(int id) {
        if (subtasks.get(id) != null) {
            historyManager.add(subtasks.get(id));
        }
        return subtasks.get(id);
    }

    //delete all subtasks, remove epic id link, update epic status
    @Override
    public void deleteAllSubtasks() {
        subtasks.values().forEach(s -> {
            Epic epic = epics.get(s.getEpicId());
            if (epic != null) {
                epic.removeSubtaskId(s.getId());
                updateEpicStatus(epic);
            }
        });
        removeTasksFromHistory(subtasks.keySet());
        subtasks.clear();
    }

    //delete subtask by id, remove epic id link, update epic status
    @Override
    public void deleteSubtaskById(int id) {
        int epicId = subtasks.get(id).getEpicId();
        epics.get(epicId).removeSubtaskId(id);
        updateEpicStatus(epics.get(epicId));
        historyManager.remove(id);
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
        epic.setId(generateId());
        updateEpicStatus(epic);
        epics.put(epic.getId(), epic);
    }

    //get epic by id
    @Override
    public Task getEpicById(int id) {
        if (epics.get(id) != null) {
            historyManager.add(epics.get(id));
        }
        return epics.get(id);
    }

    //get epics subtasks
    @Override
    public List<Subtask> getEpicSubtasksById(int id) {
        return epics.get(id).getSubtaskIds().stream().map(subtasks::get).toList();
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

    //start - earliest subtask, end - latest subtask, duration - between start/end
    private void calculateEpicDuration(Epic epic) {
        List<ZonedDateTime> startTimes = new ArrayList<>();
        List<ZonedDateTime> endTimes = new ArrayList<>();
        epic.getSubtaskIds().stream().map(subtasks::get).filter(subtask -> subtask.getStartTime() != null).forEach(subtask -> {
            startTimes.add(subtask.getStartTime());
            endTimes.add(subtask.getEndTime());
        });
        Collections.sort(startTimes);
        Collections.sort(endTimes);
        epic.calculateTime(startTimes.getFirst(), endTimes.getLast());
    }

    //get all epics
    @Override
    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    //delete all epics and all linked subtasks
    @Override
    public void deleteAllEpics() {
        removeTasksFromHistory(epics.keySet());
        epics.clear();
        removeTasksFromHistory(subtasks.keySet());
        subtasks.clear();
    }

    //delete epic by id and linked subtask
    @Override
    public void deleteEpicById(int id) {
        deleteSubtasksByIds(epics.get(id).getSubtaskIds());
        historyManager.remove(id);
        epics.remove(id);
    }

    //delete subtasks by id
    //private method only for "delete com.yandex.app.model.Epic" cases
    private void deleteSubtasksByIds(List<Integer> subtaskIds) {
        subtaskIds.forEach(id -> {
            historyManager.remove(id);
            subtasks.remove(id);
        });
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

    //remove tasks from history by id
    private void removeTasksFromHistory(Set<Integer> taskIds) {
        taskIds.forEach(historyManager::remove);
    }

    public List<Task> getPrioritisedTasks() {
        return prioritisedTasks.stream().toList();
    }
}
