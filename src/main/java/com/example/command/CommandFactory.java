package com.example.command;

import com.example.command.general.PING;
import com.example.command.string.*;
import com.example.resp.BulkString;
import com.example.resp.Resp;
import com.example.resp.RespArray;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class CommandFactory {
    private static final Map<String,Supplier<Command>> map;
    static {
        map = new HashMap<>(CommandType.values().length,1);
        for (CommandType value : CommandType.values()) {
            map.put(value.name(),value.supplier);
        }
    }
    public static Command create(RespArray respArray) {
        Resp[] argv = respArray.getArray();

        String name = ((BulkString) argv[0])
                .getContent()
                .toUtf8String()
                .toUpperCase();
        System.out.println(name);
        Supplier<Command> commandSupplier = map.get(name);
        Command command;
        if (commandSupplier == null) {
            return null;
        }
        command = commandSupplier.get();
        command.setContent(argv);
        return command;
    }
}
