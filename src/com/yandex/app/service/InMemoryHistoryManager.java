package com.yandex.app.service;

import com.yandex.app.model.Task;

import java.util.ArrayList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {

    final private List<Task> history;

    public InMemoryHistoryManager() {
        history = new ArrayList<>(10);
    }

    @Override
    public void add(Task task) {
        if (task != null) {
            if (history.size() == 10) {
                history.removeFirst();
            }
            history.add(task);
        }
    }

    @Override
    public void remove(int id) {
    }

    @Override
    public List<Task> getHistory() {
        return new ArrayList<>(history);
    }
}
