package com.example.command.result;

import com.example.resp.Resp;
import com.example.resp.RespArray;

import java.util.Arrays;
import java.util.List;

public final class ArrayResult implements CommandResult {

    private final CommandResult[] results;

    public ArrayResult(CommandResult[] results) {
        this.results = results;
    }
    public ArrayResult(List<CommandResult> results) {
        this.results = results.toArray(new CommandResult[0]);
    }

    @Override
    public Resp toResp() {
        Resp[] array = new Resp[results.length];
        for (int i = 0; i < results.length; i++) {
            array[i] = results[i].toResp();
        }
        return new RespArray(array);
    }
}