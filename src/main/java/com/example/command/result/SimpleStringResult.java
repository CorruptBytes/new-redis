package com.example.command.result;

import com.example.resp.Resp;
import com.example.resp.SimpleString;

public final class SimpleStringResult implements CommandResult {
    private final String value;
    public final static SimpleStringResult OK = new SimpleStringResult("OK");
    public final static SimpleStringResult PONG = new SimpleStringResult("PONG");
    public SimpleStringResult(String value) {
        this.value = value;
    }

    @Override
    public Resp toResp() {
        return new SimpleString(value);
    }

}
