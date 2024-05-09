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
import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {

    @Override
    protected FileBackedTaskManager createTaskManager() {
        try {
            Path taskManagerTestFile = Files.createTempFile("taskManagerTest", ".csv");
            return new FileBackedTaskManager(taskManagerTestFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void saveToFile() throws IOException {
        Path taskManagerTestFile = Files.createTempFile("taskManagerTest", ".csv");
        FileBackedTaskManager manager = new FileBackedTaskManager(taskManagerTestFile);
        ZonedDateTime dateTime = ZonedDateTime.of(2024, 4, 1, 10, 20, 10, 10, ZoneOffset.UTC);
        Task task = new Task("task1", "task description", Status.NEW);
        Epic epic = new Epic("epic1", "task description");
        Epic epic2 = new Epic("epic2", "task description");
        manager.addTask(task);
        manager.addEpic(epic);
        manager.addEpic(epic2);

        Subtask subtask1 = new Subtask("subtask1", "task description 1", epic.getId(),
                Status.NEW, Duration.ofMinutes(11), dateTime.plusHours(1));
        Subtask subtask2 = new Subtask("subtask2", "task description 1", epic.getId(),
                Status.IN_PROGRESS, Duration.ofMinutes(12), dateTime.plusHours(2));
        Subtask subtask3 = new Subtask("subtask3", "task description 1", epic.getId(),
                Status.DONE);
        manager.addSubtask(subtask1);
        manager.addSubtask(subtask2);
        manager.addSubtask(subtask3);
        manager.getTaskById(task.getId());
        manager.getEpicById(epic.getId());
        manager.getEpicById(epic2.getId());
        manager.getSubtaskById(subtask2.getId());
        String actualTaskData = Files.readString(taskManagerTestFile);
        System.out.println(actualTaskData);
        String expectedTaskData = Files.readString(Paths.get("test/resources/testTaskData_save.csv"));
        Assertions.assertEquals(expectedTaskData, actualTaskData);
        Files.deleteIfExists(taskManagerTestFile);
    }

    @Test
    void loadFromFile() {
        FileBackedTaskManager manager = FileBackedTaskManager.loadFromFile(Paths.get("test/resources/testTaskData_load.csv"));
        ZonedDateTime dateTime = ZonedDateTime.of(2024, 4, 1, 10, 20, 10, 10, ZoneOffset.UTC);
        Task task1 = new Task(1, "task1", "task description", Status.NEW);
        Task task2 = new Task(7, "task2", "task description", Status.NEW,
                Duration.ofMinutes(31), dateTime.plusDays(1).plusHours(4).plusMinutes(1).plusNanos(10));
        Epic epic1 = new Epic(2, "epic1", "task description", Status.IN_PROGRESS);
        Epic epic2 = new Epic(3, "epic2", "task description", Status.NEW);
        Subtask subtask1 = new Subtask(4, "subtask1", "task description 1", epic1.getId(), Status.NEW,
                Duration.ofMinutes(11), dateTime.plusHours(1));
        Subtask subtask2 = new Subtask(5, "subtask2", "task description 1", epic1.getId(), Status.IN_PROGRESS,
                Duration.ofMinutes(12), dateTime.plusHours(2));
        Subtask subtask3 = new Subtask(6, "subtask3", "task description 1", epic1.getId(), Status.DONE);
        Assertions.assertEquals(task1, manager.getTaskById(1));
        Assertions.assertEquals(Duration.ofMinutes(0), manager.getTaskById(1).getDuration());
        Assertions.assertNull(manager.getTaskById(1).getStartTime());

        Assertions.assertEquals(task2, manager.getTaskById(7));
        Assertions.assertEquals(task2.getDuration(), manager.getTaskById(7).getDuration());
        Assertions.assertEquals(task2.getStartTime(), manager.getTaskById(7).getStartTime());

        Assertions.assertEquals(epic1, manager.getEpicById(2));
        Assertions.assertEquals(Duration.ofMinutes(72), manager.getEpicById(2).getDuration());
        Assertions.assertEquals(dateTime.plusHours(1), manager.getEpicById(2).getStartTime());

        Assertions.assertEquals(epic2, manager.getEpicById(3));
        Assertions.assertEquals(Duration.ofMinutes(0), manager.getEpicById(3).getDuration());
        Assertions.assertNull(manager.getEpicById(3).getStartTime());

        Assertions.assertEquals(subtask1, manager.getSubtaskById(4));
        Assertions.assertEquals(subtask1.getDuration(), manager.getSubtaskById(4).getDuration());
        Assertions.assertEquals(subtask1.getStartTime(), manager.getSubtaskById(4).getStartTime());

        Assertions.assertEquals(subtask2, manager.getSubtaskById(5));
        Assertions.assertEquals(subtask2.getDuration(), manager.getSubtaskById(5).getDuration());
        Assertions.assertEquals(subtask2.getStartTime(), manager.getSubtaskById(5).getStartTime());

        Assertions.assertEquals(subtask3, manager.getSubtaskById(6));
        Assertions.assertEquals(Duration.ofMinutes(0), manager.getSubtaskById(6).getDuration());
        Assertions.assertNull(manager.getSubtaskById(6).getStartTime());
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
        task.setStartTime(ZonedDateTime.of(2024, 4, 3, 8, 1, 10, 0, ZoneOffset.UTC));
        task.setDuration(Duration.ofMinutes(25));

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
