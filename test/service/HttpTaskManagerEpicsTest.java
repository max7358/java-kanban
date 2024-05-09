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

class HttpTaskManagerEpicsTest {

    TaskManager manager = new InMemoryTaskManager(new InMemoryHistoryManager());
    HttpTaskServer taskServer = new HttpTaskServer(manager);
    Gson gson;
    private static String EPICS_BASE_URI = "http://localhost:8080/epics";

    public HttpTaskManagerEpicsTest() {
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
    void testGetEpic() throws IOException, InterruptedException {
        Epic epic = new Epic("epic", "test task");
        manager.addEpic(epic);
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(EPICS_BASE_URI + "/" + epic.getId());
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Assertions.assertEquals(200, response.statusCode());
        Epic epicFromServer = gson.fromJson(response.body(), Epic.class);
        Assertions.assertEquals(epic, epicFromServer);
    }

    @Test
    void testGetEpicNotFound() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(EPICS_BASE_URI + "/99");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Assertions.assertEquals(404, response.statusCode());
    }

    @Test
    void testGetAllEpics() throws IOException, InterruptedException {
        Epic epic1 = new Epic("task1", "test task");
        Epic epic2 = new Epic("task2", "test task");
        manager.addEpic(epic1);
        manager.addEpic(epic2);
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(EPICS_BASE_URI);
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Assertions.assertEquals(200, response.statusCode());
        JsonArray jsonElements = JsonParser.parseString(response.body()).getAsJsonArray();
        Assertions.assertEquals(2, jsonElements.size());
        Epic taskFromServer1 = gson.fromJson(jsonElements.get(0), Epic.class);
        Epic taskFromServer2 = gson.fromJson(jsonElements.get(1), Epic.class);
        Assertions.assertEquals(epic1, taskFromServer1);
        Assertions.assertEquals(epic2, taskFromServer2);
    }

    @Test
    void testPostEpic() throws IOException, InterruptedException {
        Epic task = new Epic("task", "test task");
        String taskJson = gson.toJson(task);

        //http client
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(EPICS_BASE_URI);
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();

        //rest call
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        //response
        Assertions.assertEquals(201, response.statusCode());

        List<Epic> tasksFromManager = manager.getAllEpics();

        Assertions.assertNotNull(tasksFromManager);
        Assertions.assertEquals(1, tasksFromManager.size());
        Assertions.assertEquals(task.getName(), tasksFromManager.getFirst().getName());
    }

    @Test
    void testPostUpdateEpic() throws IOException, InterruptedException {
        Epic task = new Epic("task", "test task");
        manager.addEpic(task);


        Epic taskUP = new Epic("taskUP", "test task");
        taskUP.setId(task.getId());
        String taskJson = gson.toJson(taskUP);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(EPICS_BASE_URI);
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Assertions.assertEquals(201, response.statusCode());

        List<Epic> tasksFromManager = manager.getAllEpics();
        Assertions.assertNotNull(tasksFromManager);
        Assertions.assertEquals(1, tasksFromManager.size());
        Assertions.assertEquals(taskUP, tasksFromManager.getFirst());
    }

    @Test
    void testPostUpdateEpicNotFound() throws IOException, InterruptedException {
        Epic taskUP = new Epic("taskUP", "test task",
                Status.NEW);
        taskUP.setId(1);
        String taskJson = gson.toJson(taskUP);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(EPICS_BASE_URI);
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Assertions.assertEquals(404, response.statusCode());
    }

    @Test
    void testDeleteEpic() throws IOException, InterruptedException {
        Epic task = new Epic("task", "test task");
        manager.addEpic(task);
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(EPICS_BASE_URI + "/" + task.getId());
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Assertions.assertEquals(200, response.statusCode());
        Assertions.assertEquals(0, manager.getAllEpics().size());
    }

    @Test
    void testGetEpicSubtask() throws IOException, InterruptedException {
        Epic epic = new Epic("epic", "test task");
        manager.addEpic(epic);
        Subtask subtask = new Subtask("subtask", "description", epic.getId());
        manager.addSubtask(subtask);
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(EPICS_BASE_URI + "/" + epic.getId() + "/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Assertions.assertEquals(200, response.statusCode());
        JsonArray jsonElements = JsonParser.parseString(response.body()).getAsJsonArray();
        Assertions.assertEquals(1, jsonElements.size());
        Subtask subtaskFromServer = gson.fromJson(jsonElements.get(0), Subtask.class);
        Assertions.assertEquals(subtask, subtaskFromServer);
    }

    @Test
    void testGetEpicSubtaskNotFound() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(EPICS_BASE_URI + "/10/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Assertions.assertEquals(404, response.statusCode());
    }
}