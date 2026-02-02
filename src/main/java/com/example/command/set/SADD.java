package com.example.command.set;

import com.example.command.AbstractCommand;
import com.example.command.CommandType;
import com.example.command.result.CommandResult;
import com.example.command.result.ErrorResult;
import com.example.command.result.IntegerResult;
import com.example.resp.RespArray;
import com.example.type.RedisObject;
import com.example.type.RedisSet;
import com.example.type.RedisString;
import com.example.type.RedisHash;
import com.example.database.Database;
import com.example.ServerContext;

import java.util.Arrays;

public class SADD extends AbstractCommand {

    @Override
    public CommandResult execute(ServerContext context) {
        RedisString key = stringValue(1);
        Database<RedisString, RedisObject> db = context.db();

        RedisObject obj = db.get(key);
        RedisSet set;

        if (obj == null) {
            set = new RedisSet();
            db.put(key, set);
        } else if (obj instanceof RedisSet rs) {
            set = rs;
        } else {
            return new ErrorResult("WRONGTYPE Operation against a key holding the wrong kind of value");
        }

        int added = 0;
        for (int i = 2; i < argc(); i++) {
            RedisString member = stringValue(i);
            if (set.add(member.getValue())) added++;
        }

        // AOF 持久化
        RespArray raw = new RespArray(Arrays.copyOfRange(argv, 0, argc()));
        context.aof().append(raw);

        return new IntegerResult(added);
    }

    @Override
    public CommandType type() {
        return CommandType.SADD;
    }
}
