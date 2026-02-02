package com.example.rdb;

import com.example.database.Database;
import com.example.type.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public final class RdbLoader {

    public void load(Database<RedisString, RedisObject> db, InputStream is)
            throws IOException {

        RdbInput in = new RdbInput(is);

        // header
        in.readBytes();

        while (true) {
            byte type = in.readByte();
            if (type == RdbConstants.EOF) break;

            RedisString key = in.readString();
            RedisObject value = readObject(in, type);

            db.put(key, value);

            long expireAt = in.readLong();
            if (expireAt > 0) {
                db.expire(key, expireAt - System.currentTimeMillis());
            }
        }
    }

    private RedisObject readObject(RdbInput in, byte type)
            throws IOException {

        return switch (type) {
            case RdbConstants.TYPE_STRING -> readString(in);
            case RdbConstants.TYPE_LIST   -> readList(in);
            case RdbConstants.TYPE_SET    -> readSet(in);
            case RdbConstants.TYPE_HASH   -> readHash(in);
            case RdbConstants.TYPE_ZSET   -> readZSet(in);
            default -> throw new IOException("Unknown RDB type: " + type);
        };
    }

    private RedisString readString(RdbInput in) throws IOException {
        return new RedisString(in.readBytes());
    }

    private RedisList readList(RdbInput in) throws IOException {
        int size = (int) in.readLong();
        RedisList list = new RedisList();
        for (int i = 0; i < size; i++) {
            list.rpush(new BytesWrapper(in.readBytes()));
        }
        return list;
    }

    private RedisSet readSet(RdbInput in) throws IOException {
        int size = (int) in.readLong();
        RedisSet set = new RedisSet();
        for (int i = 0; i < size; i++) {
            set.add(new BytesWrapper(in.readBytes()));
        }
        return set;
    }

    private RedisHash readHash(RdbInput in) throws IOException {
        int size = (int) in.readLong();
        RedisHash hash = new RedisHash();
        for (int i = 0; i < size; i++) {
            hash.put(
                new BytesWrapper(in.readBytes()),
                new BytesWrapper(in.readBytes())
            );
        }
        return hash;
    }

    private RedisZSet readZSet(RdbInput in) throws IOException {
        int size = (int) in.readLong();
        RedisZSet zset = new RedisZSet();
        for (int i = 0; i < size; i++) {
            long score = in.readLong();
            zset.add(List.of(
                new RedisZSet.ZsetKey(
                    new BytesWrapper(in.readBytes()), score
                )
            ));
        }
        return zset;
    }
}
