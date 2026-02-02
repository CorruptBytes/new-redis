package com.example.command.general;

import com.example.ServerContext;
import com.example.command.AbstractCommand;
import com.example.command.CommandType;
import com.example.command.result.CommandResult;
import com.example.command.result.ErrorResult;
import com.example.command.result.IntegerResult;
import com.example.database.Database;
import com.example.resp.RespInt;
import com.example.type.RedisObject;
import com.example.type.RedisString;
import io.netty.channel.ChannelHandlerContext;

public class EXPIRE extends AbstractCommand {

    @Override
    public CommandType type() {
        return CommandType.EXPIRE;
    }

    @Override
    public CommandResult execute(ServerContext context) {
        if (argc() != 3) {
            return new ErrorResult("ERR wrong number of arguments for 'expire' command");
        }

        RedisString key = stringValue(1);
        RedisObject value = context.db().get(key);

        if (value == null) {
            return new IntegerResult(0); // key 不存在

        }

        long seconds;
        try {
            seconds = longValue(2);
        } catch (NumberFormatException e) {
            return new ErrorResult("ERR value is not an integer or out of range");
        }

        context.db().expire(key, seconds * 1000); // 转换成毫秒
        return new IntegerResult(1); // 设置成功
    }
}

