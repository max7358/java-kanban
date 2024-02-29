package com.yandex.app.utility;

import com.yandex.app.service.InMemoryTaskManager;
import com.yandex.app.service.TaskManager;

public class Managers {
    public static TaskManager getDefault() {
        return new InMemoryTaskManager();
    }
}
