package com.yandex.app.http.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.yandex.app.enums.Endpoint;
import com.yandex.app.exception.NotFoundException;
import com.yandex.app.exception.ValidationException;
import com.yandex.app.model.Task;
import com.yandex.app.service.TaskManager;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class TasksHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager taskManager;

    public TasksHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Endpoint endpoint = getEndpoint(exchange.getRequestURI().getPath(), exchange.getRequestMethod());
        switch (endpoint) {
            case GET -> getTask(exchange);
            case GET_ALL -> getAllTasks(exchange);
            case POST -> postTask(exchange);
            case DELETE -> deleteTask(exchange);
            case UNKNOWN -> handleUnknown(exchange);
        }
    }

    private void deleteTask(HttpExchange exchange) throws IOException {
        int taskId = getTaskId(exchange);
        try {
            taskManager.deleteTaskById(taskId);
            sendOK(exchange);
        } catch (NotFoundException e) {
            sendNotFound(exchange, e.getMessage());
        }
    }

    private void postTask(HttpExchange exchange) throws IOException {
        String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        try {
            Task task = gson.fromJson(requestBody, Task.class);
            if (task.getId() > 0) {
                taskManager.updateTask(task);
            } else {
                taskManager.addTask(task);
            }
            sendCreated(exchange);
        } catch (ValidationException e) {
            sendHasInteractions(exchange, e.getMessage());
        } catch (NotFoundException e) {
            sendNotFound(exchange, e.getMessage());
        }
    }

    private void handleUnknown(HttpExchange exchange) throws IOException {
        sendNotFound(exchange, "Error in path!");
    }

    private void getAllTasks(HttpExchange exchange) throws IOException {
        sendText(exchange, gson.toJson(taskManager.getAllTasks()));
    }

    private void getTask(HttpExchange exchange) throws IOException, NotFoundException {
        int taskId = getTaskId(exchange);
        try {
            Task taskById = taskManager.getTaskById(taskId);
            String reply = gson.toJson(taskById);
            sendText(exchange, reply);
        } catch (NotFoundException e) {
            sendNotFound(exchange, e.getMessage());
        }
    }
}