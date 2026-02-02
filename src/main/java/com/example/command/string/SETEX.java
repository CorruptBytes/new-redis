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

public class SETEX extends AbstractCommand {

    @Override
    public CommandType type() {
        return CommandType.SETEX;
    }

    @Override
    public CommandResult execute(ServerContext context) {
        RedisString key = stringValue(1);
        long ttlSeconds = longValue(2);

        context.db().put(key, stringValue(3));
        context.db().expire(key, ttlSeconds * 1000);

        return SimpleStringResult.OK;
    }

}

