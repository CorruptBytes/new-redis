package com.example.command.general;

import com.example.ServerContext;
import com.example.command.AbstractCommand;
import com.example.command.CommandType;
import com.example.database.Database;
import com.example.command.result.BulkStringResult;
import com.example.command.result.CommandResult;
import com.example.command.result.ErrorResult;
import com.example.resp.BulkString;
import com.example.type.RedisObject;
import com.example.type.RedisString;
import io.netty.channel.ChannelHandlerContext;

public class ECHO extends AbstractCommand {
    @Override
    public CommandType type() {
        return CommandType.ECHO;
    }

    @Override
    public CommandResult execute(ServerContext context) {
        if (argv.length == 1) {
            return new ErrorResult("ERR wrong number of arguments for 'echo' command");
        }
        return new BulkStringResult(((BulkString) argv[1]).getContent().getByteArray());
    }
}
