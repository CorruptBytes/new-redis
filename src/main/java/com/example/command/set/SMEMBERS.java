package com.example.command.set;

import com.example.command.AbstractCommand;
import com.example.command.CommandType;
import com.example.command.result.ArrayResult;
import com.example.command.result.BulkStringResult;
import com.example.command.result.CommandResult;
import com.example.command.result.ErrorResult;
import com.example.database.Database;
import com.example.type.BytesWrapper;
import com.example.type.RedisObject;
import com.example.type.RedisSet;
import com.example.type.RedisString;
import com.example.ServerContext;

import java.util.ArrayList;
import java.util.List;

public class SMEMBERS extends AbstractCommand {
    @Override
    public CommandResult execute(ServerContext context) {
        RedisString key = stringValue(1);
        Database<RedisString, RedisObject> db = context.db();

        RedisObject obj = db.get(key);
        if (obj == null) return new ArrayResult(List.of()); // 空集合
        if (!(obj instanceof RedisSet set)) {
            return new ErrorResult("WRONGTYPE Operation against a key holding the wrong kind of value");
        }

        List<CommandResult> members = new ArrayList<>();
        for (BytesWrapper member : set.members()) {
            members.add(new BulkStringResult(member.getByteArray()));
        }
        return new ArrayResult(members);
    }

    @Override
    public CommandType type() {
        return CommandType.SMEMBERS;
    }
}
