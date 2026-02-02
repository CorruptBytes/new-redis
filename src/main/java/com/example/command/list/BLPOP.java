package com.example.command.list;

import com.example.ServerContext;
import com.example.command.AbstractCommand;
import com.example.command.CommandType;
import com.example.command.result.*;
import com.example.type.BytesWrapper;
import com.example.type.RedisList;
import com.example.type.RedisString;

public class BLPOP extends AbstractCommand {

    @Override
    public CommandType type() {
        return CommandType.BLPOP;
    }
    @Override
    public CommandResult execute(ServerContext ctx) {
        RedisString key = stringValue(1);
        long timeout = longValue(2);
        RedisList list = (RedisList) ctx.db().get(key);

        if (list != null && list.size() > 0) {
            BytesWrapper bw = list.lpop();
            ctx.db().put(key, list);

            return new ArrayResult(new CommandResult[]{
                    new BulkStringResult(key.getValue().getByteArray()),
                    new BulkStringResult(bw.getByteArray())
            });
        }

        // 没有元素，返回阻塞结果
        return new BlockedResult(key, true, timeout * 1000);
    }
}
