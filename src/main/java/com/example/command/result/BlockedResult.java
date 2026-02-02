package com.example.command.result;

import com.example.resp.Resp;
import com.example.type.RedisString;

public final class BlockedResult implements CommandResult {
    private final RedisString key;
    private final boolean left;
    private final long timeoutMillis;

    public BlockedResult(RedisString key, boolean left, long timeoutMillis) {
        this.key = key;
        this.left = left;
        this.timeoutMillis = timeoutMillis;
    }

    public RedisString getKey() { return key; }
    public boolean isLeft() { return left; }
    public long getTimeoutMillis() { return timeoutMillis; }

    @Override
    public Resp toResp() {
        return null;
    }
}
