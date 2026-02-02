package com.example.command.list;

import com.example.ServerContext;
import com.example.command.AbstractCommand;
import com.example.command.CommandType;
import com.example.command.result.ArrayResult;
import com.example.command.result.BulkStringResult;
import com.example.command.result.CommandResult;
import com.example.command.result.ErrorResult;
import com.example.type.BytesWrapper;
import com.example.type.RedisList;
import com.example.type.RedisObject;
import com.example.type.RedisString;

import java.util.List;

public class LRange extends AbstractCommand {

    @Override
    public CommandType type() {
        return CommandType.LRANGE;
    }

    @Override
    public CommandResult execute(ServerContext context) {
        RedisString key = stringValue(1);
        long start = longValue(2);
        long end = longValue(3);

        RedisList list = (RedisList) context.db().get(key);
        if (list == null) return new ArrayResult(new CommandResult[0]);
        if (list.type() != RedisObject.Type.LIST)
            return new ErrorResult("WRONGTYPE");

        List<BytesWrapper> values = list.range((int) start, (int) end);

        CommandResult[] res = new CommandResult[values.size()];
        for (int i = 0; i < values.size(); i++) {
            res[i] = new BulkStringResult(values.get(i).getByteArray());
        }

        return new ArrayResult(res);
    }
}
