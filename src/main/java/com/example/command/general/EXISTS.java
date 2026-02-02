package com.example.command.general;

import com.example.ServerContext;
import com.example.command.AbstractCommand;
import com.example.command.CommandType;
import com.example.command.result.CommandResult;
import com.example.command.result.ErrorResult;
import com.example.command.result.IntegerResult;
import com.example.database.Database;
import com.example.resp.Errors;
import com.example.resp.RespInt;
import com.example.type.RedisObject;
import com.example.type.RedisString;
import io.netty.channel.ChannelHandlerContext;

public class EXISTS extends AbstractCommand {

    @Override
    public CommandType type() {
        return CommandType.EXISTS;
    }

    @Override
    public CommandResult execute(ServerContext context) {
        if (argc() < 2) {
            return new ErrorResult("ERR wrong number of arguments for 'exists' command");
        }

        int count = 0;
        for (int i = 1; i < argc(); i++) {
            if (context.db().get(stringValue(i)) != null) count++;
        }
        return new IntegerResult(count);
    }
}
