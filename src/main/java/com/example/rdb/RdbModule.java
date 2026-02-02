package com.example.rdb;

import com.example.database.Database;
import com.example.type.RedisObject;
import com.example.type.RedisString;

import java.io.IOException;
import java.nio.file.Path;

public interface RdbModule {

    void save(Database<RedisString, RedisObject> db, Path file) throws IOException;

    void load(Database<RedisString, RedisObject> db, Path file) throws IOException;
}
