package com.example.rdb;

public final class RdbConstants {

    public static final byte TYPE_STRING = 0;
    public static final byte TYPE_LIST   = 1;
    public static final byte TYPE_SET    = 2;
    public static final byte TYPE_HASH   = 3;
    public static final byte TYPE_ZSET   = 4;

    public static final byte EOF = (byte) 0xFF;

    public static final byte[] HEADER =
            "REDIS-RDB-0001".getBytes();

    private RdbConstants() {}
}
