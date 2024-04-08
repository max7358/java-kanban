package service;

import com.yandex.app.enums.Status;
import com.yandex.app.model.Epic;
import com.yandex.app.model.Subtask;
import com.yandex.app.model.Task;
import com.yandex.app.service.FileBackedTaskManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

class FileBackedTaskManagerTest {
    @Test
    void saveToFile() throws IOException {
        Path taskManagerTestFile = Files.createTempFile("taskManagerTest", ".csv");
        FileBackedTaskManager manager = new FileBackedTaskManager(taskManagerTestFile);
        Task task = new Task("task1", "task description");
        Epic epic = new Epic("epic1", "task description");
        manager.addTask(task);
        manager.addEpic(epic);

        Subtask subtask1 = new Subtask("subtask1", "task description 1", epic.getId());
        Subtask subtask2 = new Subtask("subtask2", "task description 1", epic.getId(), Status.IN_PROGRESS);
        manager.addSubtask(subtask1);
        manager.addSubtask(subtask2);
        manager.getTaskById(task.getId());
        manager.getEpicById(epic.getId());
        manager.getSubtaskById(subtask2.getId());
        String actualTaskData = Files.readString(taskManagerTestFile);
        String expectedTaskData = Files.readString(Paths.get("test/resources/testTaskData_save.csv"));
        Assertions.assertEquals(expectedTaskData, actualTaskData);
        Files.deleteIfExists(taskManagerTestFile);
    }

    @Test
    void loadFromFile() {
        FileBackedTaskManager manager = FileBackedTaskManager.loadFromFile(Paths.get("test/resources/testTaskData_load.csv"));
        Task task = new Task(1, "task1", "task description", Status.NEW);
        Epic epic = new Epic(2, "epic1", "task description", Status.IN_PROGRESS);
        Subtask subtask1 = new Subtask(3, "subtask1", "task description 1", epic.getId(), Status.NEW);
        Subtask subtask2 = new Subtask(4, "subtask2", "task description 1", epic.getId(), Status.IN_PROGRESS);
        Assertions.assertEquals(task, manager.getTaskById(1));
        Assertions.assertEquals(epic, manager.getEpicById(2));
        Assertions.assertEquals(subtask1, manager.getSubtaskById(3));
        Assertions.assertEquals(subtask2, manager.getSubtaskById(4));
    }

    @Test
    void deleteTasksRemovedFromFile() throws IOException {
        Path taskManagerTestFile = Files.createTempFile("taskManagerTest", ".csv");
        FileBackedTaskManager manager = new FileBackedTaskManager(taskManagerTestFile);
        Task task = new Task("task1", "task description");
        Epic epic = new Epic("epic1", "task description");
        manager.addTask(task);
        manager.addEpic(epic);
        Subtask subtask1 = new Subtask("subtask1", "task description 1", epic.getId());
        manager.addSubtask(subtask1);
        manager.getTaskById(task.getId());
        manager.getEpicById(epic.getId());
        manager.getSubtaskById(subtask1.getId());
        manager.deleteTaskById(task.getId());
        manager.deleteEpicById(epic.getId());
        String actualTaskData = Files.readString(taskManagerTestFile);
        String expectedTaskData = Files.readString(Paths.get("test/resources/testTaskData_delete.csv"));
        Assertions.assertEquals(expectedTaskData, actualTaskData);
        Files.deleteIfExists(taskManagerTestFile);
    }

    @Test
    void deleteAllTasksRemovedFromFile() throws IOException {
        Path taskManagerTestFile = Files.createTempFile("taskManagerTest", ".csv");
        FileBackedTaskManager manager = new FileBackedTaskManager(taskManagerTestFile);
        Task task = new Task("task1", "task description");
        Epic epic = new Epic("epic1", "task description");
        manager.addTask(task);
        manager.addEpic(epic);
        Subtask subtask1 = new Subtask("subtask1", "task description 1", epic.getId());
        manager.addSubtask(subtask1);
        manager.getTaskById(task.getId());
        manager.deleteAllTasks();
        manager.deleteAllEpics();
        String actualTaskData = Files.readString(taskManagerTestFile);
        String expectedTaskData = Files.readString(Paths.get("test/resources/testTaskData_delete.csv"));
        Assertions.assertEquals(expectedTaskData, actualTaskData);
        Files.deleteIfExists(taskManagerTestFile);
    }

    @Test
    void taskUpdatesUpdateFile() throws IOException {
        Path taskManagerTestFile = Files.createTempFile("taskManagerTest", ".csv");
        FileBackedTaskManager manager = new FileBackedTaskManager(taskManagerTestFile);
        Task task = new Task("task1", "task description");
        Epic epic = new Epic("epic1", "task description");
        manager.addTask(task);
        manager.addEpic(epic);
        Subtask subtask = new Subtask("subtask1", "task description 1", epic.getId());
        manager.addSubtask(subtask);

        task.setName("new task");
        subtask.setName("new subtask");
        epic.setName("new epic");
        manager.updateSubtask(subtask);
        manager.updateTask(task);
        manager.updateEpic(epic);

        String actualTaskData = Files.readString(taskManagerTestFile);
        String expectedTaskData = Files.readString(Paths.get("test/resources/testTaskData_update.csv"));
        Assertions.assertEquals(expectedTaskData, actualTaskData);
        Files.deleteIfExists(taskManagerTestFile);
    }

    @Test
    void loadFromEmptyFile() {
        FileBackedTaskManager manager = FileBackedTaskManager.loadFromFile(Paths.get("test/resources/testTaskData_load_empty.csv"));
        Assertions.assertEquals(0, manager.getAllTasks().size());
        Assertions.assertEquals(0, manager.getAllEpics().size());
        Assertions.assertEquals(0, manager.getAllSubtasks().size());
    }
}
