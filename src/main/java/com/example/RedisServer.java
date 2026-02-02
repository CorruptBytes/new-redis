package com.example;

import com.example.database.Database;
import com.example.type.RedisObject;
import com.example.type.RedisString;

import java.util.Deque;

public interface RedisServer {
    void start();

    void close();

    Database<RedisString,RedisObject> getDatabase();
}
