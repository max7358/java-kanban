package com.yandex.app.service;

import com.yandex.app.model.Epic;
import com.yandex.app.model.Subtask;
import com.yandex.app.model.Task;
import com.yandex.app.utility.Managers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.List;

class InMemoryHistoryManagerTest {

    HistoryManager historyManager;

    @BeforeEach
    void setUp() {
        historyManager = Managers.getDefaultHistory();
    }

    @Test
    void add() {
        Task task1 = new Task("task1", "task description1");
        task1.setId(1);
        Epic epic1 = new Epic("epic1", "epic description1");
        epic1.setId(2);
        Subtask subtask1 = new Subtask("subtask1", "task description1", epic1.getId());
        subtask1.setId(3);
        historyManager.add(task1);
        historyManager.add(epic1);
        historyManager.add(subtask1);
        List<Task> history = historyManager.getHistory();
        Assertions.assertEquals(3, history.size());
        Assertions.assertTrue(history.contains(task1));
        Assertions.assertTrue(history.contains(epic1));
        Assertions.assertTrue(history.contains(subtask1));
    }

    @Test
    void remove() {
        Task task1 = new Task("task1", "task description1");
        task1.setId(1);
        Task task2 = new Task("task2", "task description2");
        task1.setId(2);
        Task task3 = new Task("task3", "task description3");
        task3.setId(3);
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);
        historyManager.remove(task2.getId());
        List<Task> history = historyManager.getHistory();
        Assertions.assertEquals(2, history.size());
        Assertions.assertTrue(history.contains(task1));
        Assertions.assertTrue(history.contains(task3));
    }

    @Test
    void historyObjectUnchanged() {
        TaskManager taskManager = Managers.getDefault();
        Task task1 = new Task("task1", "task description1");
        taskManager.addTask(task1);
        taskManager.getTaskById(task1.getId());
        List<Task> historyOld = taskManager.getHistory();

        Task taskUpdate = new Task("taskUp", "task descriptionUp");
        taskUpdate.setId(task1.getId());
        taskManager.updateTask(taskUpdate);
        taskManager.getTaskById(task1.getId());
        Assertions.assertEquals(task1, historyOld.getFirst());
    }

    @Test
    void historyOrder() {
        TaskManager taskManager = Managers.getDefault();
        Task task1 = new Task("task1", "task description1");
        Task task2 = new Task("task2", "task description2");
        Task task3 = new Task("task3", "task description3");
        Epic epic1 = new Epic("epic1", "epic description1");
        taskManager.addEpic(epic1);
        Subtask subtask1 = new Subtask("subtask1", "subtask description1", epic1.getId());
        taskManager.addSubtask(subtask1);
        taskManager.addTask(task1);
        taskManager.addTask(task2);
        taskManager.addTask(task3);
        taskManager.getTaskById(task2.getId());
        taskManager.getTaskById(task2.getId());
        taskManager.getTaskById(task1.getId());
        taskManager.getTaskById(task1.getId());
        taskManager.getTaskById(task2.getId());
        taskManager.getSubtaskById(subtask1.getId());
        taskManager.getTaskById(task3.getId());
        taskManager.getEpicById(epic1.getId());
        taskManager.getTaskById(task2.getId());
        taskManager.getSubtaskById(subtask1.getId());
        taskManager.getTaskById(task1.getId());
        taskManager.getTaskById(task3.getId());
        taskManager.getTaskById(task1.getId());
        List<Task> history = taskManager.getHistory();
        List<Task> testHistory = List.of(epic1, task2, subtask1, task3, task1);
        for (Task task : taskManager.getHistory()) {
            System.out.println(task);
        }
        Assertions.assertEquals(testHistory, history);
    }

    @Test
    void historyOrderDelete() {
        TaskManager taskManager = Managers.getDefault();
        Task task1 = new Task("task1", "task description1");
        Task task2 = new Task("task2", "task description2");
        Task task3 = new Task("task3", "task description3");
        Task task4 = new Task("task4", "task description4");
        Epic epic1 = new Epic("epic1", "epic description1");
        taskManager.addEpic(epic1);
        Subtask subtask1 = new Subtask("subtask1", "subtask description1", epic1.getId());
        taskManager.addSubtask(subtask1);
        taskManager.addTask(task1);
        taskManager.addTask(task2);
        taskManager.addTask(task3);
        taskManager.addTask(task4);
        taskManager.getTaskById(task1.getId());
        taskManager.getTaskById(task1.getId());
        taskManager.getTaskById(task4.getId());
        taskManager.getTaskById(task3.getId());
        taskManager.getTaskById(task2.getId());
        taskManager.getTaskById(task2.getId());
        taskManager.getSubtaskById(subtask1.getId());
        taskManager.getEpicById(epic1.getId());
        taskManager.getSubtaskById(subtask1.getId());
        taskManager.deleteTaskById(task2.getId());
        taskManager.getTaskById(task3.getId());
        taskManager.getTaskById(task1.getId());
        taskManager.deleteTaskById(task2.getId());
        taskManager.getTaskById(task1.getId());

        List<Task> history = taskManager.getHistory();
        List<Task> testHistory = List.of(task4, epic1, subtask1, task3, task1);
        for (Task task : taskManager.getHistory()) {
            System.out.println(task);
        }
        Assertions.assertEquals(testHistory, history);
    }

    @Test
    void historyOrderDeleteAll() {
        TaskManager taskManager = Managers.getDefault();
        Task task1 = new Task("task1", "task description1");
        Task task2 = new Task("task2", "task description2");
        Epic epic1 = new Epic("epic1", "epic description1");
        taskManager.addEpic(epic1);
        Subtask subtask1 = new Subtask("subtask1", "subtask description1", epic1.getId());
        taskManager.addSubtask(subtask1);
        taskManager.addTask(task1);
        taskManager.addTask(task2);
        taskManager.getTaskById(task1.getId());
        taskManager.getTaskById(task2.getId());
        taskManager.getSubtaskById(subtask1.getId());
        taskManager.getEpicById(epic1.getId());
        taskManager.deleteAllEpics();
        taskManager.deleteAllTasks();

        List<Task> history = taskManager.getHistory();
        Assertions.assertEquals(0, history.size());
    }
}