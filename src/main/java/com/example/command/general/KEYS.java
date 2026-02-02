package com.example.command.general;

import com.example.ServerContext;
import com.example.command.AbstractCommand;
import com.example.command.CommandType;
import com.example.command.result.ArrayResult;
import com.example.command.result.BulkStringResult;
import com.example.command.result.CommandResult;
import com.example.command.result.ErrorResult;
import com.example.database.Database;
import com.example.resp.BulkString;
import com.example.resp.Resp;
import com.example.type.RedisObject;
import com.example.type.RedisString;
import com.example.util.GlobPattern;

import java.util.List;
import java.util.regex.Pattern;

public class KEYS extends AbstractCommand {

    @Override
    public CommandType type() {
        return CommandType.KEYS;
    }

    @Override
    public CommandResult execute(ServerContext context) {

        // KEYS pattern
        if (argc() != 2) {
            return new ErrorResult("ERR wrong number of arguments for 'keys' command");
        }

        String pattern = stringValue(1).toString();

        Database<RedisString, RedisObject> db = context.db();


        List<RedisString> keys = db.keys(pattern);

        CommandResult[] results = new CommandResult[keys.size()];
        for (int i = 0; i < keys.size(); i++) {
            results[i] = new BulkStringResult(keys.get(i).getValue().getByteArray());
        }

        return new ArrayResult(results);
    }
}