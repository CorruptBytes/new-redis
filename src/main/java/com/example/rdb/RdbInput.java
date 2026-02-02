package com.example.rdb;

import com.example.type.RedisString;

import java.io.*;

public final class RdbInput {

    private final DataInputStream in;

    public RdbInput(InputStream is) {
        this.in = new DataInputStream(new BufferedInputStream(is));
    }

    public byte readByte() throws IOException {
        return in.readByte();
    }

    public long readLong() throws IOException {
        return in.readLong();
    }

    public byte[] readBytes() throws IOException {
        int len = in.readInt();
        byte[] buf = new byte[len];
        in.readFully(buf);
        return buf;
    }

    public RedisString readString() throws IOException {
        return new RedisString(readBytes());
    }
}
