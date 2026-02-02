package com.example.command.set;

import com.example.ServerContext;
import com.example.command.AbstractCommand;
import com.example.command.CommandType;
import com.example.command.result.CommandResult;
import com.example.command.result.ErrorResult;
import com.example.command.result.IntegerResult;
import com.example.type.RedisObject;
import com.example.type.RedisSet;
import com.example.type.RedisString;

public class SCARD extends AbstractCommand {
    @Override
    public CommandResult execute(ServerContext context) {
        RedisString key = stringValue(1);
        RedisObject obj = context.db().get(key);
        if (obj == null) return new IntegerResult(0);
        if (!(obj instanceof RedisSet set)) return new ErrorResult("WRONGTYPE");

        return new IntegerResult(set.size());
    }

    @Override
    public CommandType type() {
        return CommandType.SCARD;
    }
}