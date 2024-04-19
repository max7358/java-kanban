package service;

import com.yandex.app.enums.Status;
import com.yandex.app.exception.NotFoundException;
import com.yandex.app.exception.ValidationException;
import com.yandex.app.model.Epic;
import com.yandex.app.model.Subtask;
import com.yandex.app.model.Task;
import com.yandex.app.service.InMemoryHistoryManager;
import com.yandex.app.service.InMemoryTaskManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

import static com.yandex.app.enums.Status.*;

class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {

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
    void tasksPrioritizationSorting() {
        Task task1 = new Task("task1", "task description", Status.NEW, Duration.ofMinutes(30),
                ZonedDateTime.of(2024, 3, 10, 5, 0, 0, 0, ZoneOffset.UTC));
        Task task2 = new Task("task2", "task description", Status.NEW, Duration.ofMinutes(30),
                ZonedDateTime.of(2024, 3, 10, 6, 0, 0, 0, ZoneOffset.UTC));
        Task task3 = new Task("task3", "task description", Status.NEW, Duration.ofMinutes(60),
                ZonedDateTime.of(2024, 3, 10, 7, 0, 0, 0, ZoneOffset.UTC));
        Task task10 = new Task("task10", "task description", Status.NEW);
        taskManager.addTask(task3);
        taskManager.addTask(task2);
        taskManager.addTask(task10);
        taskManager.addTask(task1);
        List<Task> prioritisedTasks = taskManager.getPrioritisedTasks();
        Assertions.assertEquals(3, prioritisedTasks.size());
        Assertions.assertEquals(task1, prioritisedTasks.get(0));
        Assertions.assertEquals(task2, prioritisedTasks.get(1));
        Assertions.assertEquals(task3, prioritisedTasks.get(2));
    }

    @Test
    void tasksInterception() {
        //5:00 -> 5:30
        Task task1 = new Task("task1", "task description", Status.NEW, Duration.ofMinutes(30),
                ZonedDateTime.of(2024, 3, 10, 5, 0, 0, 0, ZoneOffset.UTC));
        //6:00->6:30
        Task task2 = new Task("task2", "task description", Status.NEW, Duration.ofMinutes(30),
                ZonedDateTime.of(2024, 3, 10, 6, 0, 0, 0, ZoneOffset.UTC));
        //5:30->6:31
        Task task3 = new Task("task3", "task description", Status.NEW, Duration.ofMinutes(31),
                ZonedDateTime.of(2024, 3, 10, 5, 30, 0, 0, ZoneOffset.UTC));
        taskManager.addTask(task1);
        taskManager.addTask(task2);
        Assertions.assertThrows(ValidationException.class, () -> taskManager.addTask(task3));
    }

    @Test
    void updateTaskNoTimeWithTime() {
        Task task0 = new Task("task0", "task description0", Status.NEW, Duration.ofMinutes(5), ZonedDateTime.now());
        Task task1 = new Task("task1", "task description1");
        Task taskUpdate = new Task("taskUp", "task descriptionUp", Status.NEW, Duration.ofMinutes(5), ZonedDateTime.now().plusHours(1));
        taskManager.addTask(task0);
        taskManager.addTask(task1);
        taskUpdate.setId(task1.getId());
        taskManager.updateTask(taskUpdate);
        Task updatedTask = taskManager.getTaskById(task1.getId());
        Assertions.assertEquals(taskUpdate, updatedTask);
        Assertions.assertEquals(2, taskManager.getPrioritisedTasks().size());
    }

    @Test
    void updateTaskWithTime() {
        Task task0 = new Task("task0", "task description0", Status.NEW, Duration.ofMinutes(5), ZonedDateTime.now());
        Task task1 = new Task("task1", "task description1", Status.NEW, Duration.ofMinutes(10), ZonedDateTime.now().plusHours(1));
        Task taskUpdate = new Task("taskUp", "task descriptionUp", Status.NEW, Duration.ofMinutes(5), ZonedDateTime.now().plusHours(2));
        taskManager.addTask(task0);
        taskManager.addTask(task1);
        taskUpdate.setId(task1.getId());
        taskManager.updateTask(taskUpdate);
        Task updatedTask = taskManager.getTaskById(task1.getId());
        Assertions.assertEquals(taskUpdate, updatedTask);
        Assertions.assertEquals(2, taskManager.getPrioritisedTasks().size());
    }

    @Test
    void updateTaskWithTimeInterception() {
        Task task0 = new Task("task0", "task description0", Status.NEW, Duration.ofMinutes(5), ZonedDateTime.now());
        Task task1 = new Task("task1", "task description1");
        Task taskUpdate = new Task("taskUp", "task descriptionUp", Status.NEW, Duration.ofMinutes(5), ZonedDateTime.now().plusMinutes(1));
        taskManager.addTask(task0);
        taskManager.addTask(task1);
        taskUpdate.setId(task1.getId());
        Assertions.assertThrows(ValidationException.class, () -> taskManager.updateTask(taskUpdate));
    }

    @Test
    void deleteTaskByIdWithTime() {
        Task task1 = new Task("task1", "task description1", Status.NEW, Duration.ofMinutes(5), ZonedDateTime.now());
        Task task2 = new Task("task2", "task description2", Status.NEW, Duration.ofMinutes(5), ZonedDateTime.now().plusMinutes(30));
        Task task3 = new Task("task3", "task description3");
        taskManager.addTask(task1);
        taskManager.addTask(task2);
        taskManager.addTask(task3);
        Assertions.assertEquals(2, taskManager.getPrioritisedTasks().size());
        taskManager.deleteTaskById(task1.getId());
        taskManager.deleteTaskById(task3.getId());
        Assertions.assertThrows(NotFoundException.class, () -> taskManager.getTaskById(task1.getId()));
        Assertions.assertThrows(NotFoundException.class, () -> taskManager.getTaskById(task3.getId()));
        Assertions.assertEquals(1, taskManager.getPrioritisedTasks().size());
    }

    @Test
    void updateSubtaskWithTime() {
        Epic epic = new Epic("epic", "epic description");
        taskManager.addEpic(epic);
        Subtask task0 = new Subtask("task0", "task description1", epic.getId(), Status.NEW, Duration.ofMinutes(5), ZonedDateTime.now());
        Subtask task1 = new Subtask("task1", "task description1", epic.getId());
        Subtask taskUpdate = new Subtask("taskUp", "task descriptionUp", epic.getId(), Status.NEW, Duration.ofMinutes(5), ZonedDateTime.now().plusHours(1));
        taskManager.addSubtask(task0);
        taskManager.addSubtask(task1);
        taskUpdate.setId(task1.getId());
        taskManager.updateSubtask(taskUpdate);
        Task updatedTask = taskManager.getSubtaskById(task1.getId());
        Assertions.assertEquals(taskUpdate, updatedTask);
        Assertions.assertEquals(2, taskManager.getPrioritisedTasks().size());
    }

    @Test
    void updateSubtaskWithTimeInterception() {
        Epic epic = new Epic("epic", "epic description");
        taskManager.addEpic(epic);
        Subtask task0 = new Subtask("task0", "task description1", epic.getId(), Status.NEW, Duration.ofMinutes(5), ZonedDateTime.now());
        Subtask task1 = new Subtask("task1", "task description1", epic.getId());
        Subtask taskUpdate = new Subtask("taskUp", "task descriptionUp", epic.getId(), Status.NEW, Duration.ofMinutes(5), ZonedDateTime.now().plusMinutes(1));
        taskManager.addTask(task0);
        taskManager.addTask(task1);
        taskUpdate.setId(task1.getId());
        Assertions.assertThrows(ValidationException.class, () -> taskManager.updateTask(taskUpdate));
    }

    @Test
    void deleteSubtaskByIdWithTime() {
        Epic epic = new Epic("epic", "epic description");
        taskManager.addEpic(epic);
        Subtask task1 = new Subtask("task0", "task description1", epic.getId(), Status.NEW, Duration.ofMinutes(5), ZonedDateTime.now());
        Subtask task2 = new Subtask("task1", "task description1", epic.getId());
        Subtask task3 = new Subtask("task1", "task description1", epic.getId(), Status.NEW, Duration.ofMinutes(5), ZonedDateTime.now().plusMinutes(5));
        taskManager.addTask(task1);
        taskManager.addTask(task2);
        taskManager.addTask(task3);
        Assertions.assertEquals(2, taskManager.getPrioritisedTasks().size());
        taskManager.deleteTaskById(task1.getId());
        taskManager.deleteTaskById(task2.getId());
        Assertions.assertThrows(NotFoundException.class, () -> taskManager.getTaskById(task1.getId()));
        Assertions.assertThrows(NotFoundException.class, () -> taskManager.getTaskById(task2.getId()));
        Assertions.assertEquals(1, taskManager.getPrioritisedTasks().size());
    }

    @Override
    protected InMemoryTaskManager createTaskManager() {
        return new InMemoryTaskManager(new InMemoryHistoryManager());
    }
}