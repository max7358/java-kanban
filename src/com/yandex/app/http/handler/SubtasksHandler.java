package com.yandex.app.http.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.yandex.app.enums.Endpoint;
import com.yandex.app.exception.NotFoundException;
import com.yandex.app.exception.ValidationException;
import com.yandex.app.model.Subtask;
import com.yandex.app.model.Task;
import com.yandex.app.service.TaskManager;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class SubtasksHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager taskManager;

    public SubtasksHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Endpoint endpoint = getEndpoint(exchange.getRequestURI().getPath(), exchange.getRequestMethod());
        switch (endpoint) {
            case GET -> getSubtask(exchange);
            case GET_ALL -> getAllSubtasks(exchange);
            case POST -> postSubtask(exchange);
            case DELETE -> deleteSubtask(exchange);
            case UNKNOWN -> handleUnknown(exchange);
        }
    }

    private void deleteSubtask(HttpExchange exchange) throws IOException {
        int taskId = getTaskId(exchange);
        try {
            taskManager.deleteSubtaskById(taskId);
            sendOK(exchange);
        } catch (NotFoundException e) {
            sendNotFound(exchange, e.getMessage());
        }
    }

    private void postSubtask(HttpExchange exchange) throws IOException {
        String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        try {
            Subtask task = gson.fromJson(requestBody, Subtask.class);
            if (task.getId() > 0) {
                taskManager.updateSubtask(task);
            } else {
                taskManager.addSubtask(task);
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

    private void getAllSubtasks(HttpExchange exchange) throws IOException {
        sendText(exchange, gson.toJson(taskManager.getAllSubtasks()));
    }

    private void getSubtask(HttpExchange exchange) throws IOException, NotFoundException {
        int taskId = getTaskId(exchange);
        try {
            Task taskById = taskManager.getSubtaskById(taskId);
            String reply = gson.toJson(taskById);
            sendText(exchange, reply);
        } catch (NotFoundException e) {
            sendNotFound(exchange, e.getMessage());
        }
    }
}