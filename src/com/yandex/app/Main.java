package com.yandex.app;

import com.yandex.app.model.Epic;
import com.yandex.app.model.Subtask;
import com.yandex.app.model.Task;
import com.yandex.app.utility.Managers;
import com.yandex.app.service.TaskManager;

public class Main {

    public static void main(String[] args) {
        TaskManager taskManager = Managers.getDefault();
        Task task1 = new Task("task1", "task description1");
        Epic epic1 = new Epic("epic1", "epic description1");
        taskManager.addTask(task1);
        taskManager.addEpic(epic1);
        Subtask subtask1 = new Subtask("subtask1", "subtask description1", epic1.getId());
        taskManager.addSubtask(subtask1);
        taskManager.getTaskById(task1.getId());
        taskManager.getSubtaskById(subtask1.getId());
        taskManager.getEpicById(epic1.getId());
        System.out.println("History:");
        for (Task task : taskManager.getHistory()) {
            System.out.println(task);
        }
    }
}
