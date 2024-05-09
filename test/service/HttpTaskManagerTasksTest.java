package service;

import com.google.gson.*;
import com.yandex.app.enums.Status;
import com.yandex.app.http.HttpTaskServer;
import com.yandex.app.http.adapter.DurationTypeAdapter;
import com.yandex.app.http.adapter.LocalTimeTypeAdapter;
import com.yandex.app.model.Task;
import com.yandex.app.service.InMemoryHistoryManager;
import com.yandex.app.service.InMemoryTaskManager;
import com.yandex.app.service.TaskManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;

class HttpTaskManagerTasksTest {

    TaskManager manager = new InMemoryTaskManager(new InMemoryHistoryManager());
    HttpTaskServer taskServer = new HttpTaskServer(manager);
    Gson gson;
    private static String TASKS_BASE_URI = "http://localhost:8080/tasks";

    public HttpTaskManagerTasksTest() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Duration.class, new DurationTypeAdapter());
        gsonBuilder.registerTypeAdapter(ZonedDateTime.class, new LocalTimeTypeAdapter());
        gson = gsonBuilder.create();
    }

    @BeforeEach
    public void setUp() {
        taskServer.start();
    }

    @AfterEach
    public void shutDown() {
        taskServer.stop();
    }

    @Test
    void testGetTask() throws IOException, InterruptedException {
        Task task = new Task("task", "test task",
                Status.NEW, Duration.ofMinutes(5), ZonedDateTime.now());
        manager.addTask(task);
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(TASKS_BASE_URI + "/" + task.getId());
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Assertions.assertEquals(200, response.statusCode());
        Task taskFromServer = gson.fromJson(response.body(), Task.class);
        Assertions.assertEquals(task, taskFromServer);
    }

    @Test
    void testGetTaskNotFound() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(TASKS_BASE_URI + "/99");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Assertions.assertEquals(404, response.statusCode());
    }

    @Test
    void testGetAllTasks() throws IOException, InterruptedException {
        Task task1 = new Task("task1", "test task",
                Status.NEW, Duration.ofMinutes(5), ZonedDateTime.now());
        Task task2 = new Task("task2", "test task",
                Status.NEW);
        manager.addTask(task1);
        manager.addTask(task2);
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(TASKS_BASE_URI);
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Assertions.assertEquals(200, response.statusCode());
        JsonArray jsonElements = JsonParser.parseString(response.body()).getAsJsonArray();
        Assertions.assertEquals(2, jsonElements.size());
        Task taskFromServer1 = gson.fromJson(jsonElements.get(0), Task.class);
        Task taskFromServer2 = gson.fromJson(jsonElements.get(1), Task.class);
        Assertions.assertEquals(task1, taskFromServer1);
        Assertions.assertEquals(task2, taskFromServer2);
    }

    @Test
    void testPostTask() throws IOException, InterruptedException {
        Task task = new Task("task", "test task",
                Status.NEW, Duration.ofMinutes(5), ZonedDateTime.now());
        String taskJson = gson.toJson(task);

        //http client
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(TASKS_BASE_URI);
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();

        //rest call
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        //response
        Assertions.assertEquals(201, response.statusCode());

        List<Task> tasksFromManager = manager.getAllTasks();

        Assertions.assertNotNull(tasksFromManager);
        Assertions.assertEquals(1, tasksFromManager.size());
        Assertions.assertEquals(task.getName(), tasksFromManager.getFirst().getName());
    }

    @Test
    void testPostUpdateTask() throws IOException, InterruptedException {
        Task task = new Task("task", "test task",
                Status.NEW, Duration.ofMinutes(5), ZonedDateTime.now());
        manager.addTask(task);


        Task taskUP = new Task("taskUP", "test task",
                Status.NEW, Duration.ofMinutes(8), task.getStartTime().plusHours(1));
        taskUP.setId(task.getId());
        String taskJson = gson.toJson(taskUP);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(TASKS_BASE_URI);
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Assertions.assertEquals(201, response.statusCode());

        List<Task> tasksFromManager = manager.getAllTasks();
        Assertions.assertNotNull(tasksFromManager);
        Assertions.assertEquals(1, tasksFromManager.size());
        Assertions.assertEquals(taskUP, tasksFromManager.getFirst());
    }

    @Test
    void testPostUpdateTaskNotFound() throws IOException, InterruptedException {
        Task taskUP = new Task("taskUP", "test task",
                Status.NEW);
        taskUP.setId(1);
        String taskJson = gson.toJson(taskUP);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(TASKS_BASE_URI);
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Assertions.assertEquals(404, response.statusCode());
    }

    @Test
    void testPostUpdateTaskIntercept() throws IOException, InterruptedException {
        Task task1 = new Task("task", "test task",
                Status.NEW, Duration.ofMinutes(5), ZonedDateTime.now());
        Task task2 = new Task("task", "test task",
                Status.NEW, Duration.ofMinutes(5), ZonedDateTime.now().plusMinutes(10));
        manager.addTask(task1);
        manager.addTask(task2);

        Task taskUP = new Task("taskUP", "test task",
                Status.NEW, Duration.ofMinutes(8), task1.getStartTime().plusMinutes(10));
        taskUP.setId(task1.getId());
        String taskJson = gson.toJson(taskUP);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(TASKS_BASE_URI);
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Assertions.assertEquals(406, response.statusCode());
    }

    @Test
    void testDeleteTask() throws IOException, InterruptedException {
        Task task = new Task("task", "test task",
                Status.NEW, Duration.ofMinutes(5), ZonedDateTime.now());
        manager.addTask(task);
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(TASKS_BASE_URI + "/" + task.getId());
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Assertions.assertEquals(200, response.statusCode());
        Assertions.assertEquals(0, manager.getAllTasks().size());
    }
}