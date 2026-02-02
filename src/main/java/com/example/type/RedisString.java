package com.example.type;

import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.Objects;
@Getter
@Setter
public class RedisString extends RedisObject {
    private BytesWrapper value;

    public RedisString(BytesWrapper bytesWrapper) {
        this.value = bytesWrapper;
        value.incrRef();
    }
    public RedisString(byte[] bytes) {
        this.value = new BytesWrapper(bytes);
        value.incrRef();
    }

    @Override
    public boolean equals(Object obj) {
        RedisString o = (RedisString) obj;
        return Objects.equals(o.value,this.value);
    }

    @Override
    public int hashCode() {
        return this.value.hashCode();
    }

    @Override
    public Type type() {
        return Type.STRING;
    }

    @Override
    public long memoryBytes() {
        return value.memoryBytes();
    }

    @Override
    public RedisObject deepCopy() {
        return new RedisString(Arrays.copyOf(value.getByteArray(), value.getByteArray().length));
    }

    @Override
    protected void free() {
        // 交给 GC
        value.decrRef();
    }

    @Override
    public String toString() {
        return value.toUtf8String();
    }
    @Override
    public long estimateWriteDelta() {
        // RedisString 覆盖整个值，相当于新写入整个 BytesWrapper 的内存
        return value == null ? 0 : value.memoryBytes();
    }

}
