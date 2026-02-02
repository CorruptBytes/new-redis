package com.example.command.hash;

import com.example.command.AbstractCommand;
import com.example.command.CommandType;
import com.example.command.result.BulkStringResult;
import com.example.command.result.CommandResult;
import com.example.command.result.ErrorResult;
import com.example.database.Database;
import com.example.type.BytesWrapper;
import com.example.type.RedisHash;
import com.example.type.RedisObject;
import com.example.type.RedisString;
import com.example.ServerContext;

public class HGET extends AbstractCommand {
    @Override
    public CommandResult execute(ServerContext context) {
        RedisString key = stringValue(1);
        BytesWrapper field = stringValue(2).getValue();

        Database<RedisString, RedisObject> db = context.db();
        RedisObject obj = db.get(key);
        if (obj == null) return BulkStringResult.NULL;
        if (!(obj instanceof RedisHash hash)) {
            return new ErrorResult("WRONGTYPE Operation against a key holding the wrong kind of value");
        }

        BytesWrapper value = hash.get(field);
        if (value == null) return BulkStringResult.NULL;
        return new BulkStringResult(value.getByteArray());
    }

    @Override
    public CommandType type() {
        return CommandType.HGET;
    }
}
