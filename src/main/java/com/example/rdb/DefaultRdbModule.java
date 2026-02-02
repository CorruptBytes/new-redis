package com.example.rdb;

import com.example.database.Database;
import com.example.type.RedisObject;
import com.example.type.RedisString;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public final class DefaultRdbModule implements RdbModule {
    @Getter
    private final RdbWriter writer = new RdbWriter();
    private final RdbLoader loader = new RdbLoader();
    @Getter
    private final RdbSaveState rdbSaveState = new RdbSaveState();
    @Override
    public void save(Database<RedisString, RedisObject> db, Path file)
            throws IOException {

        try (OutputStream os = Files.newOutputStream(file)) {
            writer.write(db, os);
        }
    }

    @Override
    public void load(Database<RedisString, RedisObject> db, Path file)
            throws IOException {

        if (!Files.exists(file)) return;

        try (InputStream is = Files.newInputStream(file)) {
            loader.load(db, is);
        }
    }
    @Setter
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RdbSaveState {
        volatile boolean bgsaveInProgress;
        volatile long lastSaveTime;
        volatile Throwable lastSaveError;
    }

}
