package com.example.command.string;

import com.example.ServerContext;
import com.example.command.AbstractCommand;
import com.example.command.CommandType;
import com.example.command.result.CommandResult;
import com.example.command.result.SimpleStringResult;
import com.example.database.Database;
import com.example.type.RedisObject;
import com.example.type.RedisString;
import io.netty.channel.ChannelHandlerContext;

public class MSET extends AbstractCommand {

    @Override
    public CommandType type() {
        return CommandType.MSET;
    }

    @Override
    public CommandResult execute(ServerContext context) {

        for (int i = 1; i < argc(); i += 2) {
            context.db().put(stringValue(i), stringValue(i + 1));
        }
        return SimpleStringResult.OK;
    }
}
