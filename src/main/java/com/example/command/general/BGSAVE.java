package com.example.command.general;

import com.example.ServerContext;
import com.example.aof.EntryView;
import com.example.command.AbstractCommand;
import com.example.command.CommandType;
import com.example.command.result.CommandResult;
import com.example.command.result.ErrorResult;
import com.example.command.result.SimpleStringResult;
import com.example.rdb.DefaultRdbModule;
import com.example.type.RedisObject;
import com.example.type.RedisString;
import com.example.util.PropertiesUtil;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class BGSAVE extends AbstractCommand {

    @Override
    public CommandType type() {
        return CommandType.BGSAVE;
    }

    @Override
    public CommandResult execute(ServerContext context) {

        DefaultRdbModule.RdbSaveState state =
                context.getRdbModule().getRdbSaveState();

        synchronized (state) {
            if (state.isBgsaveInProgress()) {
                return new ErrorResult("ERR Background save already in progress");
            }
            state.setBgsaveInProgress(true);
        }

        // 1️⃣ 创建快照（关键）
        List<EntryView<RedisString, RedisObject>> snapshot =
                context.db().entriesSnapshot();

        Path rdbFile = Paths.get(PropertiesUtil.getRdbFile());

        // 2️⃣ 后台线程
        new Thread(() -> {
            try (OutputStream os = Files.newOutputStream(rdbFile)) {

                context.getRdbModule()
                        .getWriter()
                        .writeSnapshot(snapshot, os);

                state.setLastSaveTime(System.currentTimeMillis());

            } catch (Throwable t) {
                state.setLastSaveError(t);
            } finally {
                state.setBgsaveInProgress(false);
            }
        }, "rdb-bgsave-thread").start();

        return new SimpleStringResult("Background saving started");
    }
}
