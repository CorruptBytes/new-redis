package com.example.rdb;

import com.example.type.RedisString;

import java.io.*;

public final class RdbOutput {

    private final DataOutputStream out;

    public RdbOutput(OutputStream os) {
        this.out = new DataOutputStream(new BufferedOutputStream(os));
    }

    public void writeByte(int b) throws IOException {
        out.writeByte(b);
    }

    public void writeLong(long v) throws IOException {
        out.writeLong(v);
    }

    public void writeBytes(byte[] data) throws IOException {
        out.writeInt(data.length);
        out.write(data);
    }

    public void writeString(RedisString s) throws IOException {
        writeBytes(s.getValue().getByteArray());
    }

    public void flush() throws IOException {
        out.flush();
    }
}
