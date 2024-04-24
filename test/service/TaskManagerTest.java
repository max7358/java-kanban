package service;

import com.yandex.app.exception.NotFoundException;
import com.yandex.app.model.Epic;
import com.yandex.app.model.Subtask;
import com.yandex.app.model.Task;
import com.yandex.app.service.TaskManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

abstract class TaskManagerTest<T extends TaskManager> {
    protected T taskManager;

    @BeforeEach
    public void init() {
        taskManager = createTaskManager();
    }

    protected abstract T createTaskManager();

    @Test
    void addTaskAndGetTaskById() {
        Task task1 = new Task("task1", "task description1");
        taskManager.addTask(task1);
        Task createdTask = taskManager.getTaskById(task1.getId());
        Assertions.assertEquals(task1, createdTask);
    }

    @Test
    void getAllTasks() {
        Task task1 = new Task("task1", "task description1");
        Task task2 = new Task("task2", "task description2");
        taskManager.addTask(task1);
        taskManager.addTask(task2);
        List<Task> allTasks = taskManager.getAllTasks();
        Assertions.assertEquals(2, allTasks.size());
        Assertions.assertTrue(allTasks.contains(task1));
        Assertions.assertTrue(allTasks.contains(task2));
    }

    @Test
    void deleteAllTasks() {
        Task task1 = new Task("task1", "task description1");
        Task task2 = new Task("task2", "task description2");
        taskManager.addTask(task1);
        taskManager.addTask(task2);
        taskManager.deleteAllTasks();
        List<Task> allTasks = taskManager.getAllTasks();
        Assertions.assertEquals(0, allTasks.size());
    }

    @Test
    void updateTask() {
        Task task1 = new Task("task1", "task description1");
        Task taskUpdate = new Task("taskUp", "task descriptionUp");
        taskManager.addTask(task1);
        taskUpdate.setId(task1.getId());
        taskManager.updateTask(taskUpdate);
        Task updatedTask = taskManager.getTaskById(task1.getId());
        Assertions.assertEquals(taskUpdate, updatedTask);
    }

    @Test
    void deleteTaskById() {
        Task task1 = new Task("task1", "task description1");
        taskManager.addTask(task1);
        taskManager.deleteTaskById(task1.getId());
        Assertions.assertThrows(NotFoundException.class, () -> taskManager.getTaskById(task1.getId()));
    }

    @Test
    void addSubtaskAndGetSubtaskById() {
        Epic epic1 = new Epic("epic1", "epic description1");
        taskManager.addEpic(epic1);

        Subtask subtask1 = new Subtask("subtask1", "task description1", epic1.getId());
        taskManager.addSubtask(subtask1);
        Task createdSubtask = taskManager.getSubtaskById(subtask1.getId());
        Assertions.assertEquals(subtask1, createdSubtask);
    }

    @Test
    void getAllSubtasks() {
        Epic epic1 = new Epic("epic1", "epic description1");
        taskManager.addEpic(epic1);

        Subtask subtask1 = new Subtask("subtask1", "task description1", epic1.getId());
        Subtask subtask2 = new Subtask("subtask2", "task description2", epic1.getId());
        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);
        List<Subtask> allSubtasks = taskManager.getAllSubtasks();
        Assertions.assertEquals(2, allSubtasks.size());
        Assertions.assertTrue(allSubtasks.contains(subtask1));
        Assertions.assertTrue(allSubtasks.contains(subtask2));
    }

    @Test
    void deleteAllSubtasks() {
        Epic epic1 = new Epic("epic1", "epic description1");
        taskManager.addEpic(epic1);

        Subtask subtask1 = new Subtask("subtask1", "task description1", epic1.getId());
        Subtask subtask2 = new Subtask("subtask2", "task description2", epic1.getId());
        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);
        taskManager.deleteAllSubtasks();
        List<Subtask> allSubtasks = taskManager.getAllSubtasks();
        Assertions.assertEquals(0, allSubtasks.size());
    }

    @Test
    void deleteSubtaskById() {
        Epic epic1 = new Epic("epic1", "epic description1");
        taskManager.addEpic(epic1);

        Subtask subtask1 = new Subtask("subtask1", "task description1", epic1.getId());
        taskManager.addSubtask(subtask1);
        taskManager.deleteSubtaskById(subtask1.getId());
        Assertions.assertThrows(NotFoundException.class, () -> taskManager.getSubtaskById(subtask1.getId()));
    }

    @Test
    void addEpicAndGetEpicById() {
        Epic epic1 = new Epic("epic1", "epic description1");
        taskManager.addEpic(epic1);
        taskManager.getEpicById(epic1.getId());
        Assertions.assertEquals(epic1, taskManager.getEpicById(epic1.getId()));
    }

    @Test
    void getEpicSubtasksById() {
        Epic epic1 = new Epic("epic1", "epic description1");
        taskManager.addEpic(epic1);

        Subtask subtask1 = new Subtask("subtask1", "task description1", epic1.getId());
        taskManager.addSubtask(subtask1);

        List<Subtask> subtasks = taskManager.getEpicSubtasksById(epic1.getId());
        Assertions.assertEquals(1, subtasks.size());
        Assertions.assertEquals(subtasks.getFirst(), subtask1);
    }

    @Test
    void deleteEpicById() {
        Epic epic1 = new Epic("epic1", "epic description1");
        taskManager.addEpic(epic1);
        Subtask subtask1 = new Subtask("subtask1", "task description1", epic1.getId());
        taskManager.addSubtask(subtask1);

        taskManager.deleteEpicById(epic1.getId());
        Assertions.assertNull(taskManager.getEpicById(epic1.getId()));
        Assertions.assertThrows(NotFoundException.class, () -> taskManager.getSubtaskById(epic1.getId()));
    }

    @Test
    void deleteAllEpics() {
        Epic epic1 = new Epic("epic1", "epic description1");
        Epic epic2 = new Epic("epic2", "epic description2");
        taskManager.addEpic(epic1);
        taskManager.addEpic(epic2);
        Subtask subtask1 = new Subtask("subtask1", "task description1", epic1.getId());
        Subtask subtask2 = new Subtask("subtask2", "task description2", epic2.getId());
        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);

        taskManager.deleteAllEpics();
        Assertions.assertEquals(0, taskManager.getAllEpics().size());
        Assertions.assertEquals(0, taskManager.getAllSubtasks().size());
    }
}
