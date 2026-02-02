package com.example.command.zset;

import com.example.ServerContext;
import com.example.command.AbstractCommand;
import com.example.command.CommandType;
import com.example.command.result.CommandResult;
import com.example.command.result.ErrorResult;
import com.example.command.result.IntegerResult;
import com.example.type.BytesWrapper;
import com.example.type.RedisObject;
import com.example.type.RedisString;
import com.example.type.RedisZSet;
import com.example.type.RedisZSet.ZsetKey;
import com.example.database.Database;

import java.util.ArrayList;
import java.util.List;

public class ZADD extends AbstractCommand {

    @Override
    public CommandResult execute(ServerContext context) {
        RedisString key = stringValue(1);
        Database<RedisString, RedisObject> db = context.db();

        // 获取当前对象
        RedisObject obj = db.get(key);
        RedisZSet zset;

        // 不存在 → 新建
        if (obj == null) {
            zset = new RedisZSet();
            db.put(key, zset);
        }
        // 已存在 → 检查类型并获取可写对象
        else if (obj instanceof RedisZSet rz) {
            zset = (RedisZSet) db.prepareWrite(rz);
        }
        // 类型错误
        else {
            return new ErrorResult("WRONGTYPE Operation against a key holding the wrong kind of value");
        }

        // 解析参数
        List<ZsetKey> keys = new ArrayList<>();
        for (int i = 2; i + 1 < argc(); i += 2) {
            long score;
            try {
                score = longValue(i);
            } catch (NumberFormatException e) {
                return new ErrorResult("ERR value is not an integer or out of range");
            }

            BytesWrapper member = stringValue(i + 1).getValue();
            keys.add(new ZsetKey(member, score));
        }

        int added = zset.add(keys);

        return new IntegerResult(added);
    }

    @Override
    public CommandType type() {
        return CommandType.ZADD;
    }
}
