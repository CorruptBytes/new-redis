package com.example.command.zset;

import com.example.command.AbstractCommand;
import com.example.command.CommandType;
import com.example.command.result.ArrayResult;
import com.example.command.result.BulkStringResult;
import com.example.command.result.CommandResult;
import com.example.command.result.ErrorResult;
import com.example.database.Database;
import com.example.type.RedisObject;
import com.example.type.RedisString;
import com.example.type.RedisZSet;
import com.example.ServerContext;

import java.util.List;
import java.util.stream.Collectors;

public class ZRANGE extends AbstractCommand {

    @Override
    public CommandResult execute(ServerContext context) {
        RedisString key = stringValue(1);
        int start = (int) longValue(2);
        int end = (int) longValue(3);

        Database<RedisString, RedisObject> db = context.db();
        RedisObject obj = db.get(key);
        if (obj == null) return new ArrayResult(List.of());
        if (!(obj instanceof RedisZSet zset)) {
            return new ErrorResult("WRONGTYPE Operation against a key holding the wrong kind of value");
        }

        List<RedisZSet.ZsetKey> range = zset.range(start, end);
        List<CommandResult> res = range.stream()
                .map(k -> new BulkStringResult(k.getKey().getByteArray()))
                .collect(Collectors.toList());
        return new ArrayResult(res);
    }

    @Override
    public CommandType type() {
        return CommandType.ZRANGE;
    }
}
