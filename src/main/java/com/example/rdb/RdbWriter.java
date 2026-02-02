package com.example.rdb;

import com.example.aof.EntryView;
import com.example.database.Database;
import com.example.type.*;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public final class RdbWriter {

    public void writeSnapshot(
            List<EntryView<RedisString, RedisObject>> snapshot,
            OutputStream os
    ) throws IOException {

        RdbOutput out = new RdbOutput(os);

        // HEADER
        out.writeBytes(RdbConstants.HEADER);

        for (EntryView<RedisString, RedisObject> ev : snapshot) {
            writeEntry(out, ev);
        }
        out.writeByte(RdbConstants.EOF);
        out.flush();
    }

    public void write(Database<RedisString, RedisObject> db, OutputStream os)
            throws IOException {

        // 标记 COW（非常关键）
        db.markAllObjectsCow();
        writeSnapshot(db.entriesSnapshot(),os);
    }
    private void writeEntry(RdbOutput out,
                            EntryView<RedisString, RedisObject> ev)
            throws IOException {

        RedisObject obj = ev.value();

        switch (obj.type()) {
            case STRING -> writeString(out, ev.key(), (RedisString) obj);
            case LIST   -> writeList(out, ev.key(), (RedisList) obj);
            case SET    -> writeSet(out, ev.key(), (RedisSet) obj);
            case HASH   -> writeHash(out, ev.key(), (RedisHash) obj);
            case ZSET   -> writeZSet(out, ev.key(), (RedisZSet) obj);
        }

        // expire
        out.writeLong(ev.expireAt() == null ? -1 : ev.expireAt());
    }

    private void writeString(RdbOutput out, RedisString key, RedisString val)
            throws IOException {

        out.writeByte(RdbConstants.TYPE_STRING);
        out.writeString(key);
        out.writeBytes(val.getValue().getByteArray());
    }

    private void writeList(RdbOutput out, RedisString key, RedisList list)
            throws IOException {

        out.writeByte(RdbConstants.TYPE_LIST);
        out.writeString(key);
        out.writeLong(list.size());

        for (BytesWrapper bw : list.range(0, -1)) {
            out.writeBytes(bw.getByteArray());
        }
    }

    private void writeSet(RdbOutput out, RedisString key, RedisSet set)
            throws IOException {

        out.writeByte(RdbConstants.TYPE_SET);
        out.writeString(key);
        out.writeLong(set.size());

        for (BytesWrapper bw : set.members()) {
            out.writeBytes(bw.getByteArray());
        }
    }

    private void writeHash(RdbOutput out, RedisString key, RedisHash hash)
            throws IOException {

        out.writeByte(RdbConstants.TYPE_HASH);
        out.writeString(key);
        out.writeLong(hash.size());

        for (var e : hash.entries()) {
            out.writeBytes(e.getKey().getByteArray());
            out.writeBytes(e.getValue().getByteArray());
        }
    }

    private void writeZSet(RdbOutput out, RedisString key, RedisZSet zset)
            throws IOException {

        out.writeByte(RdbConstants.TYPE_ZSET);
        out.writeString(key);
        out.writeLong(zset.size());

        for (RedisZSet.ZsetKey zk : zset.range(0, zset.size() - 1)) {
            out.writeLong(zk.getScore());
            out.writeBytes(zk.getKey().getByteArray());
        }
    }


}
