package com.yandex.app.service;

import com.yandex.app.model.Epic;
import com.yandex.app.model.Subtask;
import com.yandex.app.model.Task;
import com.yandex.app.utility.Managers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {

    HistoryManager historyManager;

    @BeforeEach
    void setUp() {
        historyManager = Managers.getDefaultHistory();
    }

    @Test
    void add() {
        Task task1 = new Task("task1", "task description1");
        Epic epic1 = new Epic("epic1", "epic description1");
        Subtask subtask1 = new Subtask("subtask1", "task description1", epic1.getId());
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
    void historySize10() {
        for (int i = 0; i < 15; i++) {
            historyManager.add(new Task("task" + 1, "task description"));
        }
        List<Task> history = historyManager.getHistory();
        Assertions.assertEquals(10, history.size());
    }
}