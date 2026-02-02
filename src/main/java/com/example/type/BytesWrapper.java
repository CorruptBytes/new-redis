package com.example.type;

import java.util.Arrays;

public class BytesWrapper extends RedisObject implements MemoryTrackable,Comparable<BytesWrapper> {
    private final byte[] content;

    public BytesWrapper(String value) {
        // 使用 UTF-8 编码，和 Redis 一致
        this.content = value.getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }
    public BytesWrapper(byte[] content) {
        // 使用 UTF-8 编码，和 Redis 一致
        this.content = content;
    }

    public byte[] getByteArray() {
        return content;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BytesWrapper that = (BytesWrapper) o;
        return Arrays.equals(content, that.content);
    }

    public String toUtf8String() {
        return new String(content, java.nio.charset.StandardCharsets.UTF_8);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(content);
    }

    @Override
    public int compareTo(BytesWrapper o) {
        final int len1 = content.length;
        final int len2 = o.getByteArray().length;
        final int lim = Math.min(len1, len2);
        byte[] v2 = o.getByteArray();

        int k = 0;
        while (k < lim) {
            final byte c1 = content[k];
            final byte c2 = v2[k];
            if (c1 != c2) {
                return c1 - c2;
            }
            k++;
        }
        return len1 - len2;
    }

    @Override
    public Type type() {
        return null;
    }

    @Override
    protected void free() {

    }

    @Override
    public RedisObject deepCopy() {
        return null;
    }

    @Override
    public long memoryBytes() {
        // 对象头 + 长度 + 字节数组所占字节数
        return content == null ? 0 : 4 + 16 + content.length;
    }
}