package com.yandex.app.service;

public class IdGenerator {
    private static int idSeq = 0;

    public static int generateId(){
        return idSeq++;
    }
}
