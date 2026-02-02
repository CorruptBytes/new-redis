package com.example.command.zset;

import com.example.ServerContext;
import com.example.command.AbstractCommand;
import com.example.command.CommandType;
import com.example.command.result.CommandResult;
import com.example.command.result.ErrorResult;
import com.example.command.result.IntegerResult;
import com.example.type.BytesWrapper;
import com.example.type.RedisObject;
import com.example.type.RedisString;
import com.example.type.RedisZSet;

import java.util.ArrayList;
import java.util.List;

public class ZREM extends AbstractCommand {
    @Override
    public CommandResult execute(ServerContext context) {
        RedisString key = stringValue(1);
        RedisObject obj = context.db().get(key);
        if (obj == null) return new IntegerResult(0);
        if (!(obj instanceof RedisZSet zset)) return new ErrorResult("WRONGTYPE");

        if (zset.isShared()) {
            zset = (RedisZSet) context.db().prepareWrite(zset);
            context.db().put(key, zset);
        }

        List<BytesWrapper> members = new ArrayList<>();
        for (int i = 2; i < argc(); i++) members.add(stringValue(i).getValue());
        int removed = zset.remove(members);
        return new IntegerResult(removed);
    }

    @Override
    public CommandType type() {
        return CommandType.ZREM;
    }
}