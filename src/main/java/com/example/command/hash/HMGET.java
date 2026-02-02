package com.example.command.hash;

import com.example.ServerContext;
import com.example.command.AbstractCommand;
import com.example.command.CommandType;
import com.example.command.result.ArrayResult;
import com.example.command.result.BulkStringResult;
import com.example.command.result.CommandResult;
import com.example.command.result.ErrorResult;
import com.example.type.BytesWrapper;
import com.example.type.RedisHash;
import com.example.type.RedisObject;
import com.example.type.RedisString;

import java.util.ArrayList;
import java.util.List;

public class HMGET extends AbstractCommand {

    @Override
    public CommandResult execute(ServerContext context) {
        RedisString key = stringValue(1);
        RedisObject obj = context.db().get(key);

        if (obj == null) return new ArrayResult(List.of());
        if (!(obj instanceof RedisHash hash)) return new ErrorResult("WRONGTYPE");

        List<CommandResult> res = new ArrayList<>();
        for (int i = 2; i < argc(); i++) {
            BytesWrapper field = stringValue(i).getValue();
            BytesWrapper value = hash.get(field);
            res.add(value == null ? BulkStringResult.NULL : new BulkStringResult(value.getByteArray()));
        }

        return new ArrayResult(res);
    }

    @Override
    public CommandType type() {
        return CommandType.HMGET;
    }
}
