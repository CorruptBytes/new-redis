package com.example.command.list;

import com.example.ServerContext;
import com.example.command.AbstractCommand;
import com.example.command.CommandType;
import com.example.command.result.BulkStringResult;
import com.example.command.result.CommandResult;
import com.example.command.result.ErrorResult;
import com.example.type.BytesWrapper;
import com.example.type.RedisList;
import com.example.type.RedisObject;
import com.example.type.RedisString;

public class LPop extends AbstractCommand {

    @Override
    public CommandType type() {
        return CommandType.LPOP;
    }

    @Override
    public CommandResult execute(ServerContext context) {
        RedisString key = stringValue(1);

        RedisList list = (RedisList) context.db().get(key);
        if (list == null) return BulkStringResult.NULL;
        if (list.type() != RedisObject.Type.LIST)
            return new ErrorResult("WRONGTYPE");

        list = (RedisList) context.db().prepareWrite(list);
        BytesWrapper v = list.lpop();

        if (list.size() == 0) {
            context.db().remove(key);
        } else {
            context.db().put(key, list);
        }

        return v == null
                ? BulkStringResult.NULL
                : new BulkStringResult(v.getByteArray());
    }
}
