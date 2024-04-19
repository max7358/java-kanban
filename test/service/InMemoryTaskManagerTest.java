package service;

import com.yandex.app.enums.Status;
import com.yandex.app.exception.NotFoundException;
import com.yandex.app.exception.ValidationException;
import com.yandex.app.model.Epic;
import com.yandex.app.model.Subtask;
import com.yandex.app.model.Task;
import com.yandex.app.service.TaskManager;
import com.yandex.app.utility.Managers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.yandex.app.enums.Status.*;

class InMemoryTaskManagerTest {
    TaskManager taskManager;

    @BeforeEach
    void setUp() {
        taskManager = Managers.getDefault();
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
    void addTaskAndGetTaskById() {
        Task task1 = new Task("task1", "task description1");
        taskManager.addTask(task1);
        Task createdTask = taskManager.getTaskById(task1.getId());
        Assertions.assertEquals(task1, createdTask);
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
        Task deletedSubtask = taskManager.getSubtaskById(subtask1.getId());
        Assertions.assertNull(deletedSubtask);
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
        Assertions.assertNull(taskManager.getSubtaskById(epic1.getId()));
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

    @Test
    void updateEpicStatus() {
        Epic epic1 = new Epic("epic1", "epic description1");
        taskManager.addEpic(epic1);
        Assertions.assertEquals(NEW, taskManager.getEpicById(epic1.getId()).getStatus());

        Subtask subtask1 = new Subtask("subtask1", "subtask description1", epic1.getId());
        Subtask subtask2 = new Subtask("subtask2", "subtask description2", epic1.getId());
        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);
        Assertions.assertEquals(NEW, taskManager.getEpicById(epic1.getId()).getStatus());

        Subtask updateSubtask1 = new Subtask("subtask update1", "subtask description update", subtask1.getEpicId(), Status.NEW);
        updateSubtask1.setId(subtask1.getId());
        Subtask updateSubtask2 = new Subtask("subtask update2", "subtask description update", subtask2.getEpicId(), Status.DONE);
        updateSubtask2.setId(subtask2.getId());
        taskManager.updateSubtask(updateSubtask1);
        taskManager.updateSubtask(updateSubtask2);

        Assertions.assertEquals(IN_PROGRESS, taskManager.getEpicById(epic1.getId()).getStatus());

        updateSubtask1.setStatus(DONE);
        taskManager.updateSubtask(updateSubtask1);
        Assertions.assertEquals(DONE, taskManager.getEpicById(epic1.getId()).getStatus());
    }

    @Test
    void taskEndTimeCalculation() {
        ZonedDateTime dateTime = ZonedDateTime.of(2024, 2, 28, 23, 20, 0, 0, ZoneOffset.UTC);
        ZonedDateTime expectedDateTime = ZonedDateTime.of(2024, 2, 29, 0, 21, 0, 0, ZoneOffset.UTC);
        Task task = new Task("task1", "task description", Status.NEW, Duration.ofMinutes(61), dateTime);
        Assertions.assertEquals(expectedDateTime, task.getEndTime());
    }

    @Test
    void epicTimeCalculation() {
        ZonedDateTime dateTime = ZonedDateTime.of(2024, 2, 28, 23, 20, 0, 0, ZoneOffset.UTC);
        Epic epic = new Epic("epic1", "task description");
        taskManager.addEpic(epic);

        Subtask subtask1 = new Subtask("subtask1", "task description 1", epic.getId(),
                Status.NEW, Duration.ofMinutes(10), dateTime.minusHours(1));
        Subtask subtask2 = new Subtask("subtask2", "task description 1", epic.getId(),
                Status.IN_PROGRESS, Duration.ofMinutes(25), dateTime.plusHours(2));
        Subtask subtask3 = new Subtask("subtask3", "task description 1", epic.getId(),
                Status.DONE);
        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);
        taskManager.addSubtask(subtask3);

        ZonedDateTime expectedStartTime = ZonedDateTime.of(2024, 2, 28, 22, 20, 0, 0, ZoneOffset.UTC);
        ZonedDateTime expectedEndTime = ZonedDateTime.of(2024, 2, 29, 1, 45, 0, 0, ZoneOffset.UTC);
        Assertions.assertEquals(expectedStartTime, epic.getStartTime());
        Assertions.assertEquals(expectedEndTime, epic.getEndTime());
        Assertions.assertEquals(Duration.ofMinutes(205), epic.getDuration());
    }

    @Test
    void sortedTasks() {
        //5:00 -> 5:30
        Task task1 = new Task("task1", "task description", Status.NEW, Duration.ofMinutes(30),
                ZonedDateTime.of(2024, 3, 10, 5, 0, 0, 0, ZoneOffset.UTC));
        //6:00->6:30
        Task task2 = new Task("task2", "task description", Status.NEW, Duration.ofMinutes(30),
                ZonedDateTime.of(2024, 3, 10, 6, 0, 0, 0, ZoneOffset.UTC));
        //7:00->8:00
        Task task3 = new Task("task3", "task description", Status.NEW, Duration.ofMinutes(60),
                ZonedDateTime.of(2024, 3, 10, 7, 0, 0, 0, ZoneOffset.UTC));

        Task task4 = new Task("task4", "task description", Status.NEW, Duration.ofMinutes(10000),
                ZonedDateTime.of(2024, 3, 10, 7, 59, 0, 0, ZoneOffset.UTC));
        Task task10 = new Task("task10", "task description", Status.NEW);
        taskManager.addTask(task1);
        taskManager.addTask(task2);
        taskManager.addTask(task3);
        taskManager.addTask(task10);
        Assertions.assertThrows(ValidationException.class, () -> taskManager.addTask(task4));
    }
}