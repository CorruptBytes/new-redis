package com.example.command.string;

import com.example.ServerContext;
import com.example.command.AbstractCommand;
import com.example.command.CommandType;
import com.example.command.result.ArrayResult;
import com.example.command.result.BulkStringResult;
import com.example.command.result.CommandResult;
import com.example.database.Database;
import com.example.resp.BulkString;
import com.example.resp.Resp;
import com.example.resp.RespArray;
import com.example.type.RedisObject;
import com.example.type.RedisString;
import io.netty.channel.ChannelHandlerContext;

public class MGET extends AbstractCommand {

    @Override
    public CommandType type() {
        return CommandType.MGET;
    }

    @Override
    public CommandResult execute(ServerContext context) {

        CommandResult[] result = new CommandResult[argc() - 1];

        for (int i = 1; i < argc(); i++) {
            RedisObject value = context.db().get(stringValue(i));
            if (value == null) {
                result[i - 1] = BulkStringResult.NULL;
            } else {
                RedisString str = (RedisString) value;
                result[i - 1] = new BulkStringResult(str.getValue().getByteArray());
            }
        }
        return new ArrayResult(result);
    }
}

