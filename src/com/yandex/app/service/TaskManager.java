package com.yandex.app.service;

import com.yandex.app.model.Epic;
import com.yandex.app.model.Subtask;
import com.yandex.app.model.Task;

import java.util.ArrayList;
import java.util.List;

public interface TaskManager {
    //Task methods
    //get all tasks
    List<Task> getAllTasks();

    //delete all tasks
    void deleteAllTasks();

    //add task
    Task addTask(Task task);

    //update task
    Task updateTask(Task task);

    //get task by id
    Task getTask(int id);

    //delete task by id
    Task deleteTask(int id);

    //subtask methods
    //add subtask, assume it can't exist w/o epic
    void addSubtask(Subtask subtask);

    //get all subtasks
    List<Subtask> getAllSubtasks();

    //get subtask by id
    Task getSubtask(int id);

    //delete all subtasks, remove epic id link, update epic status
    void deleteAllSubtasks();

    //delete subtask by id, remove epic id link, update epic status
    void deleteSubtask(int id);

    //update subtask, update epic status
    Subtask updateSubtask(Subtask subtask);

    //epic methods
    //add epic
    void addEpic(Epic epic);

    //get epic by id
    Task getEpic(int id);

    //get epics subtasks
    ArrayList<Subtask> getEpicSubtasks(int id);

    //get all epics
    List<Epic> getAllEpics();

    //delete all epics and all linked subtasks
    void deleteAllEpics();

    //delete epic by id and linked subtask
    void deleteEpic(int id);

    //update epic
    Epic updateEpic(Epic epic);

    //return last 10 viewed tasks
    List<Task> getHistory();
}
