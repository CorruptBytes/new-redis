package com.example.command.zset;

import com.example.ServerContext;
import com.example.command.AbstractCommand;
import com.example.command.CommandType;
import com.example.command.result.CommandResult;
import com.example.command.result.ErrorResult;
import com.example.command.result.IntegerResult;
import com.example.type.RedisObject;
import com.example.type.RedisString;
import com.example.type.RedisZSet;

public class ZCARD extends AbstractCommand {
    @Override
    public CommandResult execute(ServerContext context) {
        RedisString key = stringValue(1);
        RedisObject obj = context.db().get(key);
        if (obj == null) return new IntegerResult(0);
        if (!(obj instanceof RedisZSet zset)) return new ErrorResult("WRONGTYPE");

        return new IntegerResult(zset.size());
    }

    @Override
    public CommandType type() {
        return CommandType.ZCARD;
    }
}