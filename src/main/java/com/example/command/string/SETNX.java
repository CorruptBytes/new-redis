package com.example.command.string;

import com.example.ServerContext;
import com.example.command.AbstractCommand;
import com.example.command.CommandType;
import com.example.command.result.CommandResult;
import com.example.command.result.IntegerResult;
import com.example.database.Database;
import com.example.resp.RespInt;
import com.example.type.RedisObject;
import com.example.type.RedisString;
import io.netty.channel.ChannelHandlerContext;

public class SETNX extends AbstractCommand {

    @Override
    public CommandType type() {
        return CommandType.SETNX;
    }

    @Override
    public CommandResult execute(ServerContext context) {
        RedisString key = stringValue(1);

        if (context.db().get(key) != null) {
            return new IntegerResult(0);
        }

        context.db().put(key, stringValue(2));
        return new IntegerResult(1);
    }

}
