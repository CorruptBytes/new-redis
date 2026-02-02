package com.example.command.general;

import com.example.ServerContext;
import com.example.command.AbstractCommand;
import com.example.command.CommandType;
import com.example.command.result.BulkStringResult;
import com.example.command.result.CommandResult;
import com.example.command.result.SimpleStringResult;
import com.example.database.Database;
import com.example.resp.BulkString;
import com.example.resp.SimpleString;
import com.example.type.RedisObject;
import com.example.type.RedisString;
import io.netty.channel.ChannelHandlerContext;

public class PING extends AbstractCommand {

    @Override
    public CommandType type() {
        return CommandType.PING;
    }

    @Override
    public CommandResult execute(ServerContext context) {
        return argc() >= 2 ? new BulkStringResult(((BulkString) argv[1]).getContent().getByteArray()): SimpleStringResult.PONG;
    }
}
