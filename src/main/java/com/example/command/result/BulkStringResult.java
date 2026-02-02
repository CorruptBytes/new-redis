package com.example.command.result;

import com.example.resp.BulkString;
import com.example.resp.Resp;

public final class BulkStringResult implements CommandResult {

    public static final CommandResult NULL = new BulkStringResult(null);
    private final byte[] value; // null 表示 $-1

    public BulkStringResult(byte[] value) {
        this.value = value;
    }

    @Override
    public Resp toResp() {
        return value == null
                ? BulkString.NULL
                : new BulkString(value);
    }

}
