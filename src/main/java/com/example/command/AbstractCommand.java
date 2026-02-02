package com.example.command;

import com.example.resp.BulkString;
import com.example.resp.Errors;
import com.example.resp.Resp;
import com.example.resp.SimpleString;
import com.example.type.RedisObject;
import com.example.type.RedisString;
import io.netty.channel.ChannelHandlerContext;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractCommand
        implements Command<RedisString, RedisObject> {

    public Resp[] argv;
    private Map<Integer, RedisString> stringValueCache;

    @Override
    public void setContent(Resp[] array) {
        this.argv = array;
        this.stringValueCache = new HashMap<>(array.length);
    }

    protected int argc() {
        return argv.length;
    }


    /** argv[i] → Redis string value */
    protected RedisString stringValue(int i) {
        return stringValueCache.computeIfAbsent(i, index -> {
            return new RedisString(
                    ((BulkString) argv[index]).getContent()
            );
        });
    }

    protected long longValue(int i) {
        return Long.parseLong(
                ((BulkString) argv[i]).getContent().toUtf8String()
        );
    }

}
