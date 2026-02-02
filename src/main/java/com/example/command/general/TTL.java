package com.example.command.general;

import com.example.ServerContext;
import com.example.command.AbstractCommand;
import com.example.command.CommandType;
import com.example.command.result.CommandResult;
import com.example.command.result.ErrorResult;
import com.example.command.result.IntegerResult;
import com.example.database.Database;
import com.example.resp.RespInt;
import com.example.type.RedisObject;
import com.example.type.RedisString;
import io.netty.channel.ChannelHandlerContext;

public class TTL extends AbstractCommand {

    @Override
    public CommandType type() {
        return CommandType.TTL;
    }

    @Override
    public CommandResult execute(ServerContext context) {
        if (argc() != 2) {
            return new ErrorResult("ERR wrong number of arguments for 'ttl' command");
        }

        RedisString key = stringValue(1);
        RedisObject value = context.db().get(key);

        if (value == null) {
            return new IntegerResult(-2);// key 不存在
        }
        Long expireAt = context.db().getExpireTime(key); // 需要在 Database 中实现获取 expire 时间的方法
        if (expireAt == null) {
            return new IntegerResult(-1); // key 没有过期时间
        }

        long ttl = (expireAt - System.currentTimeMillis()) / 1000;
        ttl = Math.max(ttl, 0);
        return new IntegerResult(ttl);
    }
}
