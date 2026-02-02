package com.example.command.string;

import com.example.ServerContext;
import com.example.command.AbstractCommand;
import com.example.command.CommandType;
import com.example.command.result.CommandResult;
import com.example.command.result.ErrorResult;
import com.example.command.result.IntegerResult;
import com.example.database.Database;
import com.example.resp.RespInt;
import com.example.type.BytesWrapper;
import com.example.type.RedisObject;
import com.example.type.RedisString;
import io.netty.channel.ChannelHandlerContext;

public class INCR extends AbstractCommand {

    @Override
    public CommandType type() {
        return CommandType.INCR;
    }

    @Override
    public CommandResult execute(ServerContext context) {
        RedisString key = stringValue(1);

        RedisObject obj = context.db().get(key);
        long num;

        if (obj == null) {
            // key 不存在：等价于 SET key 1
            num = 1;
            context.db().put(key, new RedisString(new BytesWrapper("1")));
            return new IntegerResult(num);
        }

        // key 存在，类型检查
        if (obj.type() != RedisObject.Type.STRING) {
            return new ErrorResult(
                    "WRONGTYPE Operation against a key holding the wrong kind of value"
            );
        }

        // ===== COW 关键点 =====
        RedisString str = (RedisString) context.db().prepareWrite(obj);

        // 解析旧值
        try {
            num = Long.parseLong(str.getValue().toUtf8String());
        } catch (NumberFormatException e) {
            return new ErrorResult("ERR value is not an integer or out of range");
        }

        // INCR / INCRBY
        if (argc() >= 3) {
            long delta = longValue(2);
            num += delta;
        } else {
            num++;
        }

        // 原地修改 value
        str.setValue(new BytesWrapper(Long.toString(num)));

        // ⚠️ 必须 put 回去（因为 prepareWrite 可能返回的是 copy）
        context.db().put(key, str);

        return new IntegerResult(num);
    }
}

