package com.yandex.app;

import com.yandex.app.enums.Status;
import com.yandex.app.model.Epic;
import com.yandex.app.model.Subtask;
import com.yandex.app.model.Task;
import com.yandex.app.utility.Managers;
import com.yandex.app.service.TaskManager;

public class Main {

    public static void main(String[] args) {
        Task task1 = new Task("task1", "task description1");
        Task task2 = new Task("task2", "task description2");
        Task updateTask = new Task("task updated", "task description updated");

        Epic epic1 = new Epic("epic1", "epic description1");
        Epic epic2 = new Epic("epic2", "epic description2");


        TaskManager taskManager = Managers.getDefault();

        taskManager.addTask(task1);
        Task addedTask = taskManager.addTask(task2);
        updateTask.setId(addedTask.getId());
        taskManager.updateTask(updateTask);
        //System.out.println(taskManager.getAllTasks());


        taskManager.addEpic(epic1);
        taskManager.addEpic(epic2);

        Epic updateEpic = new Epic("epic update", "epic description update");
        updateEpic.setId(epic2.getId());
        taskManager.updateEpic(updateEpic);

        Subtask subtask1 = new Subtask("subtask1","subtask description1", epic1.getId());
        Subtask subtask2 = new Subtask("subtask2","subtask description2", epic1.getId());
        Subtask subtask3 = new Subtask("subtask3","subtask description3", epic2.getId());

        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);
        taskManager.addSubtask(subtask3);
        //System.out.println(taskManager.getAllSubtasks());
        //System.out.println(taskManager.getAllEpics());

        Subtask updateSubtask1 = new Subtask("subtask update1","subtask description update", subtask1.getEpicId(), Status.NEW);
        updateSubtask1.setId(subtask1.getId());

        Subtask updateSubtask2 = new Subtask("subtask update2","subtask description update", subtask2.getEpicId(), Status.DONE);
        updateSubtask2.setId(subtask2.getId());
        taskManager.updateSubtask(updateSubtask1);
        taskManager.updateSubtask(updateSubtask2);

        //System.out.println(taskManager.getAllEpics());
        //System.out.println(taskManager.getEpicSubtasks(epic1.getId()));

        //taskManager.deleteEpic(epic1.getId());
        //taskManager.deleteSubtask(subtask2.getId());
        System.out.println("---------------------------------");
        //System.out.println(taskManager.getAllSubtasks());

        //taskManager.deleteSubtask(subtask2.getId());
        //taskManager.deleteSubtask(subtask1.getId());
        //System.out.println(taskManager.getAllEpics());

        taskManager.getTask(task1.getId());
        taskManager.getTask(task2.getId());
        taskManager.getSubtask(subtask1.getId());
        taskManager.getEpic(epic1.getId());
        taskManager.getTask(task2.getId());
        System.out.println("History:");
        for (Task task : taskManager.getHistory()) {
            System.out.println(task);
        }
    }
}
