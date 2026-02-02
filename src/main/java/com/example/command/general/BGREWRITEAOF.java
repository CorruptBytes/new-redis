package com.example.command.general;

import com.example.ServerContext;
import com.example.aof.AofManager;
import com.example.command.AbstractCommand;
import com.example.command.CommandType;
import com.example.command.result.CommandResult;
import com.example.command.result.SimpleStringResult;
import com.example.database.Database;
import com.example.evnetloop.MainEventLoop;
import com.example.type.RedisObject;
import com.example.type.RedisString;
import io.netty.channel.EventLoop;
import io.netty.channel.EventLoopGroup;

public class BGREWRITEAOF extends AbstractCommand {
    @Override
    public CommandType type() {
        return CommandType.BGREWRITEAOF;
    }

    @Override
    public CommandResult execute(ServerContext context) {
        AofManager aof = context.aof();
        EventLoopGroup mainGroup = context.main();

        // 取主 EventLoop（通常是 group.next()）
        EventLoop mainLoop = mainGroup.next();

        aof.startRewrite(mainLoop);

        // Redis 行为：立即返回 OK，不等待完成
        return new SimpleStringResult("OK");
    }
}
