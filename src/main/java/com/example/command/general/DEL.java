package com.example.command.general;

import com.example.ServerContext;
import com.example.command.AbstractCommand;
import com.example.command.CommandType;
import com.example.database.Database;
import com.example.command.result.CommandResult;
import com.example.command.result.ErrorResult;
import com.example.command.result.IntegerResult;
import com.example.type.RedisObject;
import com.example.type.RedisString;

public class DEL extends AbstractCommand {

    @Override
    public CommandType type() {
        return CommandType.DEL;
    }

    @Override
    public CommandResult execute(ServerContext context) {
        // 至少一个键
        if (argc() < 2) {
            return new ErrorResult("ERR wrong number of arguments for 'del' command");
        }

        int removed = 0;
        for (int i = 1; i < argv.length; i++) {
            RedisString key = stringValue(i);
            if (context.db().remove(key)) { // remove 返回被删除的对象
                removed++;
            }
        }

        return new IntegerResult(removed);
    }
}

