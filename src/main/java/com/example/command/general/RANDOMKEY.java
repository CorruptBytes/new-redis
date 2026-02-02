package com.example.command.general;

import com.example.ServerContext;
import com.example.command.AbstractCommand;
import com.example.command.CommandType;
import com.example.command.result.BulkStringResult;
import com.example.command.result.CommandResult;
import com.example.database.Database;
import com.example.type.RedisObject;
import com.example.type.RedisString;


public class RANDOMKEY extends AbstractCommand {

    @Override
    public CommandType type() {
        return CommandType.RANDOMKEY;
    }

    @Override
    public CommandResult execute(ServerContext context) {
        RedisString key = context.db().randomKey();
        if (key == null) {
            return BulkStringResult.NULL;
        } else {
            return new BulkStringResult(key.getValue().getByteArray());
        }
    }
}
