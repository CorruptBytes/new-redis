package com.example.command.result;

import com.example.resp.Resp;
import com.example.resp.RespInt;

public final class IntegerResult implements CommandResult {
    private final long value;

    public IntegerResult(long value) {
        this.value = value;
    }

    @Override
    public Resp toResp() {
        return new RespInt(value);
    }
}
