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

public class SET extends AbstractCommand {

    @Override
    public CommandType type() {
        return CommandType.SET;
    }

    @Override
    public CommandResult execute(ServerContext context) {
        context.db().put(stringValue(1), stringValue(2));
        return SimpleStringResult.OK;
    }

}
