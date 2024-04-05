package com.yandex.app.service;

public class ManagerSaveException extends RuntimeException {
    public ManagerSaveException(Exception e) {
        super(e);
    }
}
