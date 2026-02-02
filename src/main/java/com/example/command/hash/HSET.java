package com.example.command.hash;


import com.example.command.AbstractCommand;
import com.example.command.CommandType;
import com.example.command.result.CommandResult;
import com.example.command.result.IntegerResult;
import com.example.command.result.ErrorResult;
import com.example.database.Database;
import com.example.resp.RespArray;
import com.example.type.BytesWrapper;
import com.example.type.RedisHash;
import com.example.type.RedisObject;
import com.example.type.RedisString;
import com.example.ServerContext;

public class HSET extends AbstractCommand {

    @Override
    public CommandResult execute(ServerContext context) {
        RedisString key = stringValue(1);
        Database<RedisString, RedisObject> db = context.db();

        RedisObject obj = db.get(key);
        RedisHash hash;

        if (obj == null) {
            hash = new RedisHash();
            db.put(key, hash);
        } else if (obj instanceof RedisHash rh) {
            hash = rh;
        } else {
            return new ErrorResult("WRONGTYPE Operation against a key holding the wrong kind of value");
        }

        int updated = 0;
        for (int i = 2; i + 1 < argc(); i += 2) {
            BytesWrapper field = stringValue(i).getValue();
            BytesWrapper value = stringValue(i + 1).getValue();
            if (hash.get(field) == null) updated++;
            hash.put(field, value);
        }

        // 持久化
        RespArray raw = new RespArray(argv);
        context.aof().append(raw);

        return new IntegerResult(updated);
    }

    @Override
    public CommandType type() {
        return CommandType.HSET;
    }
}
