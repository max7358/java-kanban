package com.yandex.app.service;

import com.yandex.app.model.Node;
import com.yandex.app.model.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {

    private final HashMap<Integer, Node> history;
    private Node tail;
    private Node head;

    public InMemoryHistoryManager() {
        history = new HashMap<>();
    }

    public void add(Task task) {
        Node node = history.get(task.getId());
        if (node != null) {
            removeNode(node);
        }
        linkLast(task);
        history.put(task.getId(), tail);
    }

    private void linkLast(Task task) {
        final Node node = new Node(tail, task, null);
        if (tail != null) {
            tail.next = node;
        }
        if (head == null) {
            head = node;
        }
        tail = node;
    }

    private List<Task> getTasks() {
        List<Task> tasks = new ArrayList<>();
        Node node = head;
        while (node != null) {
            tasks.add(node.data);
            node = node.next;
        }
        return tasks;
    }

    private void removeNode(Node node) {
        Node prev = node.prev;
        Node next = node.next;

        if (prev == null && next == null) {
            head = null;
            tail = null;
        } else if (prev != null && next != null) {
            prev.next = next;
            next.prev = prev;
        } else if (prev == null) {
            next.prev = null;
            head = next;
        } else {
            prev.next = null;
            tail = prev;
        }
    }

    @Override
    public void remove(int id) {
        Node node = history.get(id);
        if (node != null) {
            removeNode(node);
            history.remove(id);
        }
    }

    @Override
    public List<Task> getHistory() {
        return new ArrayList<>(getTasks());
    }
}
