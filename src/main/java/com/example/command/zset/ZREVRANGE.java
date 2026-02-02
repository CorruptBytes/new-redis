package com.example.command.zset;

import com.example.ServerContext;
import com.example.command.AbstractCommand;
import com.example.command.CommandType;
import com.example.command.result.ArrayResult;
import com.example.command.result.BulkStringResult;
import com.example.command.result.CommandResult;
import com.example.command.result.ErrorResult;
import com.example.type.RedisObject;
import com.example.type.RedisString;
import com.example.type.RedisZSet;

import java.util.List;

public class ZREVRANGE extends AbstractCommand {
    @Override
    public CommandResult execute(ServerContext context) {
        RedisString key = stringValue(1);
        int start = (int) longValue(2);
        int end = (int) longValue(3);

        RedisObject obj = context.db().get(key);
        if (obj == null) return new ArrayResult(List.of());
        if (!(obj instanceof RedisZSet zset)) return new ErrorResult("WRONGTYPE");

        List<RedisZSet.ZsetKey> range = zset.reRange(start, end);
        List<CommandResult> res = range.stream()
                .map(k -> (CommandResult) new BulkStringResult(k.getKey().getByteArray()))
                .toList();

        return new ArrayResult(res);
    }

    @Override
    public CommandType type() {
        return CommandType.ZREVRANGE;
    }
}