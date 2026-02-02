package com.example.aof;

import com.example.resp.BulkString;
import com.example.resp.Resp;
import com.example.resp.RespArray;
import com.example.type.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class AofCommandBuilder {

    private AofCommandBuilder() {
    }

    /**
     * 将一个 EntryView 转换为 AOF 所需的 RESP 命令序列
     */
    public static List<RespArray> build(EntryView<RedisString, RedisObject> entry) {

        List<RespArray> cmds = new ArrayList<>();

        RedisString key = entry.key();
        RedisObject value = entry.value();

        switch (value.type()) {
            case STRING -> buildString(cmds, key, (RedisString) value);
            case LIST   -> buildList(cmds, key, (RedisList) value);
            case SET    -> buildSet(cmds, key, (RedisSet) value);
            case HASH   -> buildHash(cmds, key, (RedisHash) value);
            case ZSET   -> buildZSet(cmds, key, (RedisZSet) value);
            default -> throw new UnsupportedOperationException(
                    "AOF rewrite not supported type: " + value.type()
            );
        }

        // 过期时间
        if (entry.expireAt() != null) {
            long ttl = entry.expireAt() - System.currentTimeMillis();
            if (ttl > 0) {
                cmds.add(buildPexpire(key, ttl));
            }
        }

        return cmds;
    }
    private static void buildString(
            List<RespArray> out,
            RedisString key,
            RedisString value) {

        RespArray set = new RespArray(new Resp[] {
                new BulkString("SET".getBytes()),
                new BulkString(key),
                new BulkString(value)
        });

        out.add(set);
    }
    private static void buildList(
            List<RespArray> out,
            RedisString key,
            RedisList list) {

        int size = list.size();
        if (size == 0) {
            return;
        }

        Resp[] argv = new Resp[size + 2];
        argv[0] = new BulkString("RPUSH".getBytes());
        argv[1] = new BulkString(key);

        int i = 2;
        for (BytesWrapper bw : list.range(0, -1)) {
            argv[i++] = new BulkString(bw.getByteArray());
        }

        out.add(new RespArray(argv));
    }
    private static void buildSet(List<RespArray> out, RedisString key, RedisSet set) {
        if (set.size() == 0) return;

        Resp[] argv = new Resp[set.size() + 2];
        argv[0] = new BulkString("SADD".getBytes());
        argv[1] = new BulkString(key);

        int i = 2;
        for (BytesWrapper bw : set.members()) {
            argv[i++] = new BulkString(bw.getByteArray());
        }

        out.add(new RespArray(argv));
    }

    private static void buildHash(List<RespArray> out, RedisString key, RedisHash hash) {
        if (hash.size() == 0) return;

        Resp[] argv = new Resp[hash.size() * 2 + 2];
        argv[0] = new BulkString("HMSET".getBytes());
        argv[1] = new BulkString(key);
        int i = 2;
        for (Map.Entry<BytesWrapper, BytesWrapper> entry : hash.entries()) {
            argv[i++] = new BulkString(entry.getKey().getByteArray());
            argv[i++] = new BulkString(entry.getValue().getByteArray());

        }

        out.add(new RespArray(argv));
    }

    private static void buildZSet(List<RespArray> out, RedisString key, RedisZSet zset) {
        if (zset.size() == 0) return;

        Resp[] argv = new Resp[zset.size() * 2 + 2];
        argv[0] = new BulkString("ZADD".getBytes());
        argv[1] = new BulkString(key);

        int i = 2;
        // 这里使用 0 到 size-1 的全量范围
        for (RedisZSet.ZsetKey zkey : zset.range(0, zset.size() - 1)) {
            argv[i++] = new BulkString(Long.toString(zkey.getScore()).getBytes());
            argv[i++] = new BulkString(zkey.getKey().getByteArray());
        }

        out.add(new RespArray(argv));
    }

    private static RespArray buildPexpire(
            RedisString key,
            long ttlMillis) {

        return new RespArray(new Resp[] {
                new BulkString("PEXPIRE".getBytes()),
                new BulkString(key),
                new BulkString(Long.toString(ttlMillis).getBytes())
        });
    }
}

