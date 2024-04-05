package com.yandex.app.service;

import com.yandex.app.enums.Status;
import com.yandex.app.enums.Type;
import com.yandex.app.model.Epic;
import com.yandex.app.model.Subtask;
import com.yandex.app.model.Task;
import com.yandex.app.utility.Managers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private static final String CSV_FILE_NAME = "src/resources/taskManagerData.csv";
    private static final String CSV_TITLE = "id,type,name,status,description,epic";
    private final Path path;

    public FileBackedTaskManager(HistoryManager historyManager, Path path) {
        super(historyManager);
        this.path = path;
    }

    public FileBackedTaskManager(HistoryManager historyManager) {
        this(historyManager, Paths.get(CSV_FILE_NAME));
    }

    public FileBackedTaskManager(Path path) {
        this(Managers.getDefaultHistory(), path);
    }

    public static FileBackedTaskManager loadFromFile(Path file) {
        FileBackedTaskManager fileBackedTaskManager = new FileBackedTaskManager(file);
        fileBackedTaskManager.init();
        return fileBackedTaskManager;
    }

    private void init() {
        loadFromFile();
    }

    private void loadFromFile() {
        if (Files.exists(path)) {
            try (BufferedReader br = Files.newBufferedReader(path)) {
                int maxId = 0;
                //skip header
                br.readLine();
                while (true) {
                    String line = br.readLine();
                    if (line != null && !line.isEmpty()) {
                        Task task = fromString(line);
                        int id = task.getId();
                        if (id > maxId) {
                            maxId = id;
                        }
                        fillTasks(task);
                    } else break;
                }
                idSeq = maxId;

                String line = br.readLine();
                if (line != null && !line.isEmpty()) {
                    List<Integer> ids = historyFromString(line);
                    fillHistory(ids);
                }
            } catch (IOException e) {
                throw new ManagerSaveException(e);
            }
        }
    }

    private void fillTasks(Task task) {
        switch (task.getType()) {
            case TASK -> tasks.put(task.getId(), task);
            case EPIC -> epics.put(task.getId(), (Epic) task);
            case SUBTASK -> {
                subtasks.put(task.getId(), (Subtask) task);
                epics.get(((Subtask) task).getEpicId()).addSubtaskId(task.getId());
            }
        }
    }

    private void fillHistory(List<Integer> ids) {
        Map<Integer, Task> tasks = getAllTypesOfTasks().stream().collect(Collectors.toMap(Task::getId, Function.identity()));
        ids.forEach(id -> historyManager.add(tasks.get(id)));
    }

    private Task fromString(String line) {
        String[] strings = line.split(",");
        String id = strings[0];
        Type type = Type.valueOf(strings[1]);
        String name = strings[2];
        Status status = Status.valueOf(strings[3]);
        String description = strings[4];
        Task task = null;
        switch (type) {
            case TASK -> task = new Task(Integer.parseInt(id), name, description, status);
            case EPIC -> task = new Epic(Integer.parseInt(id), name, description, status);
            case SUBTASK ->
                    task = new Subtask(Integer.parseInt(id), name, description, Integer.parseInt(strings[5]), status);
        }
        return task;
    }

    static List<Integer> historyFromString(String value) {
        return Arrays.stream(value.split(",")).map(Integer::valueOf).toList();
    }

    private List<Task> getAllTypesOfTasks() {
        List<Task> tasks = new ArrayList<>();
        tasks.addAll(getAllTasks());
        tasks.addAll(getAllEpics());
        tasks.addAll(getAllSubtasks());
        return tasks;
    }

    private void save() {
        try (BufferedWriter bw = Files.newBufferedWriter(path)) {
            bw.write(CSV_TITLE);
            bw.newLine();

            for (Task t : getAllTypesOfTasks()) {
                bw.write(toString(t));
                bw.newLine();
            }
            bw.newLine();
            bw.write(historyToString(historyManager));
        } catch (IOException e) {
            throw new ManagerSaveException(e);
        }
    }

    String toString(Task task) {
        StringBuilder sb = new StringBuilder();
        sb.append(task.getId()).append(",")
                .append(task.getType()).append(",")
                .append(task.getName()).append(",")
                .append(task.getStatus()).append(",")
                .append(task.getDescription()).append(",");
        if (task.getType() == Type.SUBTASK) {
            sb.append(((Subtask) task).getEpicId());
        }
        return sb.toString();
    }

    static String historyToString(HistoryManager historyManager) {
        StringBuilder sb = new StringBuilder();
        historyManager.getHistory().forEach(task -> sb.append(task.getId()).append(","));
        return sb.toString();
    }

    @Override
    public Task addTask(Task task) {
        super.addTask(task);
        save();
        return task;
    }

    @Override
    public void addSubtask(Subtask task) {
        super.addSubtask(task);
        save();
    }

    @Override
    public void addEpic(Epic task) {
        super.addEpic(task);
        save();
    }

    @Override
    public Task getTaskById(int id) {
        Task task = super.getTaskById(id);
        if (task != null) {
            save();
        }
        return task;
    }

    @Override
    public Task getSubtaskById(int id) {
        Task task = super.getSubtaskById(id);
        if (task != null) {
            save();
        }
        return task;
    }

    @Override
    public Task getEpicById(int id) {
        Task task = super.getEpicById(id);
        if (task != null) {
            save();
        }
        return task;
    }
}
