package com.example.command.list;

import com.example.ServerContext;
import com.example.command.AbstractCommand;
import com.example.command.CommandType;
import com.example.command.result.CommandResult;
import com.example.command.result.ErrorResult;
import com.example.command.result.IntegerResult;
import com.example.resp.BulkString;
import com.example.type.BytesWrapper;
import com.example.type.RedisList;
import com.example.type.RedisObject;
import com.example.type.RedisString;

public class LPush extends AbstractCommand {

    @Override
    public CommandType type() {
        return CommandType.LPUSH;
    }

    @Override
    public CommandResult execute(ServerContext ctx) {
        RedisString key = stringValue(1);

        RedisList list = (RedisList) ctx.db().get(key);
        if (list == null) {
            list = new RedisList();
        } else {
            list = (RedisList) ctx.db().prepareWrite(list);
        }

        for (int i = 2; i < argc(); i++) {
            list.lpush(((BulkString) argv[i]).getContent());
        }
        int size = list.size();
        ctx.db().putAndSignal(key, list);

        return new IntegerResult(size);
    }
}
