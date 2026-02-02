package com.example.command.set;

import com.example.ServerContext;
import com.example.command.AbstractCommand;
import com.example.command.CommandType;
import com.example.command.result.CommandResult;
import com.example.command.result.ErrorResult;
import com.example.command.result.IntegerResult;
import com.example.type.RedisObject;
import com.example.type.RedisSet;
import com.example.type.RedisString;

public class SREM extends AbstractCommand {
    @Override
    public CommandResult execute(ServerContext context) {
        RedisString key = stringValue(1);
        RedisObject obj = context.db().get(key);

        if (obj == null) return new IntegerResult(0);
        if (!(obj instanceof RedisSet set)) return new ErrorResult("WRONGTYPE");

        if (set.isShared()) {
            set = (RedisSet) context.db().prepareWrite(set);
            context.db().put(key, set);
        }

        int removed = 0;
        for (int i = 2; i < argc(); i++) {
            if (set.contains(stringValue(i).getValue())) {
                set.remove(stringValue(i).getValue());
                removed++;
            }
        }

        return new IntegerResult(removed);
    }

    @Override
    public CommandType type() {
        return CommandType.SREM;
    }
}