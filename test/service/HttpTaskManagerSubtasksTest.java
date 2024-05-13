package service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.yandex.app.enums.Status;
import com.yandex.app.http.HttpTaskServer;
import com.yandex.app.http.adapter.DurationTypeAdapter;
import com.yandex.app.http.adapter.LocalTimeTypeAdapter;
import com.yandex.app.model.Epic;
import com.yandex.app.model.Subtask;
import com.yandex.app.service.InMemoryHistoryManager;
import com.yandex.app.service.InMemoryTaskManager;
import com.yandex.app.service.TaskManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;

class HttpTaskManagerSubtasksTest {

    TaskManager manager = new InMemoryTaskManager(new InMemoryHistoryManager());
    HttpTaskServer taskServer = new HttpTaskServer(manager);
    Gson gson;
    Epic epic = new Epic("epic", "description");
    static String SUBTASKS_BASE_URI = "http://localhost:8080/subtasks";

    public HttpTaskManagerSubtasksTest() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Duration.class, new DurationTypeAdapter());
        gsonBuilder.registerTypeAdapter(ZonedDateTime.class, new LocalTimeTypeAdapter());
        gson = gsonBuilder.create();
    }

    @BeforeEach
    public void setUp() {
        taskServer.start();
        manager.addEpic(epic);
    }

    @AfterEach
    public void shutDown() {
        taskServer.stop();
    }

    @Test
    void testGetSubtask() throws IOException, InterruptedException {
        Subtask task = new Subtask("subtask", "test subtask", epic.getId(),
                Status.NEW, Duration.ofMinutes(5), ZonedDateTime.now());
        manager.addSubtask(task);
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(SUBTASKS_BASE_URI + "/" + task.getId());
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Assertions.assertEquals(200, response.statusCode());
        Subtask taskFromServer = gson.fromJson(response.body(), Subtask.class);
        Assertions.assertEquals(task, taskFromServer);
    }

    @Test
    void testGetSubtaskNotFound() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(SUBTASKS_BASE_URI + "/99");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Assertions.assertEquals(404, response.statusCode());
    }

    @Test
    void testGetAllSubtasks() throws IOException, InterruptedException {
        Subtask task1 = new Subtask("subtask", "test subtask", epic.getId(),
                Status.NEW, Duration.ofMinutes(5), ZonedDateTime.now());
        Subtask task2 = new Subtask("task2", "test task", epic.getId(),
                Status.NEW);
        manager.addSubtask(task1);
        manager.addSubtask(task2);
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(SUBTASKS_BASE_URI);
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Assertions.assertEquals(200, response.statusCode());
        JsonArray jsonElements = JsonParser.parseString(response.body()).getAsJsonArray();
        Assertions.assertEquals(2, jsonElements.size());
        Subtask taskFromServer1 = gson.fromJson(jsonElements.get(0), Subtask.class);
        Subtask taskFromServer2 = gson.fromJson(jsonElements.get(1), Subtask.class);
        Assertions.assertEquals(task1, taskFromServer1);
        Assertions.assertEquals(task2, taskFromServer2);
    }

    @Test
    void testPostSubtask() throws IOException, InterruptedException {
        Subtask task = new Subtask("task", "test task", epic.getId(),
                Status.NEW, Duration.ofMinutes(5), ZonedDateTime.now());
        String taskJson = gson.toJson(task);

        //http client
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(SUBTASKS_BASE_URI);
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();

        //rest call
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        //response
        Assertions.assertEquals(201, response.statusCode());

        List<Subtask> tasksFromManager = manager.getAllSubtasks();
        Assertions.assertNotNull(tasksFromManager);
        Assertions.assertEquals(1, tasksFromManager.size());
        Assertions.assertEquals(task.getName(), tasksFromManager.getFirst().getName());
    }

    @Test
    void testPostUpdateSubtask() throws IOException, InterruptedException {
        Subtask task = new Subtask("task", "test task",epic.getId(),
                Status.NEW, Duration.ofMinutes(5), ZonedDateTime.now());
        manager.addSubtask(task);


        Subtask taskUP = new Subtask("taskUP", "test task", epic.getId(),
                Status.NEW, Duration.ofMinutes(8), task.getStartTime().plusHours(1));
        taskUP.setId(task.getId());
        String taskJson = gson.toJson(taskUP);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(SUBTASKS_BASE_URI);
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Assertions.assertEquals(201, response.statusCode());

        List<Subtask> tasksFromManager = manager.getAllSubtasks();
        Assertions.assertNotNull(tasksFromManager);
        Assertions.assertEquals(1, tasksFromManager.size());
        Assertions.assertEquals(taskUP, tasksFromManager.getFirst());
    }

    @Test
    void testPostUpdateSubtaskNotFound() throws IOException, InterruptedException {
        Subtask taskUP = new Subtask("taskUP", "test task", epic.getId(),
                Status.NEW);
        taskUP.setId(1);
        String taskJson = gson.toJson(taskUP);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(SUBTASKS_BASE_URI);
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Assertions.assertEquals(404, response.statusCode());
    }

    @Test
    void testPostUpdateSubtaskIntercept() throws IOException, InterruptedException {
        Subtask task1 = new Subtask("task", "test task",epic.getId(),
                Status.NEW, Duration.ofMinutes(5), ZonedDateTime.now());
        Subtask task2 = new Subtask("task", "test task",epic.getId(),
                Status.NEW, Duration.ofMinutes(5), ZonedDateTime.now().plusMinutes(10));
        manager.addSubtask(task1);
        manager.addSubtask(task2);

        Subtask taskUP = new Subtask("taskUP", "test task", epic.getId(),
                Status.NEW, Duration.ofMinutes(8), task1.getStartTime().plusMinutes(10));
        taskUP.setId(task1.getId());
        String taskJson = gson.toJson(taskUP);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(SUBTASKS_BASE_URI);
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Assertions.assertEquals(406, response.statusCode());
    }

    @Test
    void testDeleteSubtask() throws IOException, InterruptedException {
        Subtask task = new Subtask("task", "test task", epic.getId(),
                Status.NEW, Duration.ofMinutes(5), ZonedDateTime.now());
        manager.addSubtask(task);
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(SUBTASKS_BASE_URI + "/" + task.getId());
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Assertions.assertEquals(200, response.statusCode());
        Assertions.assertEquals(0, manager.getAllSubtasks().size());
    }
}