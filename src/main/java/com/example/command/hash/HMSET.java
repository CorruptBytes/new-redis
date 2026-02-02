package com.example.command.hash;

import com.example.ServerContext;
import com.example.command.AbstractCommand;
import com.example.command.CommandType;
import com.example.command.result.CommandResult;
import com.example.command.result.ErrorResult;
import com.example.command.result.SimpleStringResult;
import com.example.type.RedisHash;
import com.example.type.RedisObject;
import com.example.type.RedisString;

public class HMSET extends AbstractCommand {

    @Override
    public CommandResult execute(ServerContext context) {
        if ((argc() - 2) % 2 != 0) {
            return new ErrorResult("ERR wrong number of arguments for 'hmset'");
        }

        RedisString key = stringValue(1);
        RedisObject obj = context.db().get(key);

        RedisHash hash;
        if (obj == null) {
            hash = new RedisHash();
            context.db().put(key, hash);
        } else if (obj instanceof RedisHash rh) {
            hash = rh;
        } else {
            return new ErrorResult("WRONGTYPE");
        }

        // 遵循 COW 设计
        if (hash.isShared()) {
            hash = (RedisHash) context.db().prepareWrite(hash);
            context.db().put(key, hash);
        }

        for (int i = 2; i + 1 < argc(); i += 2) {
            hash.put(stringValue(i).getValue(), stringValue(i + 1).getValue());
        }

        return SimpleStringResult.OK;
    }

    @Override
    public CommandType type() {
        return CommandType.HMSET;
    }
}
