package com.example.command.string;

import com.example.ServerContext;
import com.example.command.AbstractCommand;
import com.example.command.CommandType;
import com.example.command.result.BulkStringResult;
import com.example.command.result.CommandResult;
import com.example.command.result.ErrorResult;
import com.example.database.Database;
import com.example.resp.BulkString;
import com.example.type.RedisObject;
import com.example.type.RedisString;
import io.netty.channel.ChannelHandlerContext;

public class GET extends AbstractCommand {

    @Override
    public CommandType type() {
        return CommandType.GET;
    }

    @Override
    public CommandResult execute(ServerContext context) {

        RedisObject value = context.db().get(stringValue(1));

        if (value == null) {
            return BulkStringResult.NULL;
        }

        if (value.type() != RedisObject.Type.STRING) {
            return new ErrorResult("WRONGTYPE Operation against a key holding the wrong kind of value");
        }

        RedisString str = (RedisString) value;
        return new BulkStringResult(str.getValue().getByteArray());
    }
}
