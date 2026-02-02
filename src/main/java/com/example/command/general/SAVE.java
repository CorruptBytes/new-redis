package com.example.command.general;

import com.example.ServerContext;
import com.example.command.AbstractCommand;
import com.example.command.CommandType;
import com.example.command.result.CommandResult;
import com.example.command.result.ErrorResult;
import com.example.command.result.SimpleStringResult;
import com.example.util.PropertiesUtil;

import java.nio.file.Path;
import java.nio.file.Paths;

public class SAVE extends AbstractCommand {

    @Override
    public CommandType type() {
        return CommandType.SAVE;
    }

    @Override
    public CommandResult execute(ServerContext context) {

        try {
            Path rdbFile = Paths.get(PropertiesUtil.getRdbFile());

            context.getRdbModule()
                    .save(context.db(), rdbFile);

            context.getRdbModule()
                    .getRdbSaveState()
                    .setLastSaveTime(System.currentTimeMillis());

            return SimpleStringResult.OK;

        } catch (Exception e) {
            return new ErrorResult("ERR save failed: " + e.getMessage());
        }
    }
}
