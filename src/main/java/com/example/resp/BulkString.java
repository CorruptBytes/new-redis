package com.example.resp;

import com.example.type.BytesWrapper;
import com.example.type.RedisString;
import lombok.Getter;

@Getter
public class BulkString implements Resp {

    public static final BulkString NULL = new BulkString((BytesWrapper) null);

    private final BytesWrapper content;

    public BulkString(byte[] bytes) {
        this.content = new BytesWrapper(bytes);
    }
    public BulkString(BytesWrapper content) {
        if (content == null) {
            this.content = null;
            return;
        }
        this.content = content;
    }
    public BulkString(RedisString content) {
        if (content == null) {
            this.content = null;
            return;
        }
        this.content = content.getValue();
    }
    public boolean isNull() {
        return content == null;
    }
}
