package com.yandex.app.service;

public class IdGenerator {
    private int idSeq = 0;

    public int generateId() {
        return idSeq++;
    }
}