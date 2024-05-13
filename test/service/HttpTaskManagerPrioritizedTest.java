package service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.yandex.app.enums.Status;
import com.yandex.app.http.HttpTaskServer;
import com.yandex.app.http.adapter.DurationTypeAdapter;
import com.yandex.app.http.adapter.LocalTimeTypeAdapter;
import com.yandex.app.model.Task;
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

class HttpTaskManagerPrioritizedTest {

    TaskManager manager = new InMemoryTaskManager(new InMemoryHistoryManager());
    HttpTaskServer taskServer = new HttpTaskServer(manager);
    Gson gson;
    private static String PRIORITIZED_BASE_URI = "http://localhost:8080/prioritized";

    public HttpTaskManagerPrioritizedTest() {
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
    void testGetPrioritized() throws IOException, InterruptedException {
        Task task1 = new Task("task1", "test task",
                Status.NEW, Duration.ofMinutes(5), ZonedDateTime.now());
        Task task2 = new Task("task2", "test task",
                Status.NEW, Duration.ofMinutes(5), ZonedDateTime.now().plusMinutes(10));
        Task task3 = new Task("task3", "test task",
                Status.NEW, Duration.ofMinutes(5), ZonedDateTime.now().minusMinutes(10));
        manager.addTask(task1);
        manager.addTask(task2);
        manager.addTask(task3);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(PRIORITIZED_BASE_URI);
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Assertions.assertEquals(200, response.statusCode());
        JsonArray jsonElements = JsonParser.parseString(response.body()).getAsJsonArray();
        Assertions.assertEquals(3, jsonElements.size());
        Assertions.assertEquals(task1, gson.fromJson(jsonElements.get(1), Task.class));
    }
}