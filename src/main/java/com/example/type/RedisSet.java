package com.example.type;

import java.util.HashSet;

public class RedisSet extends RedisObject {

    private HashSet<BytesWrapper> set;
    private long memory;

    public RedisSet() {
        this.set = new HashSet<>();
        this.memory = 0;
    }

    public RedisSet(HashSet<BytesWrapper> source, long memory) {
        this.set = source;
        this.memory = memory;
    }

    private void ensureWritable() {
        if (isShared()) {
            set = new HashSet<>(set);
            clearCow();
        }
    }

    private BytesWrapper retain(BytesWrapper bw) {
        bw.incrRef();
        memory += bw.memoryBytes();
        return bw;
    }

    private BytesWrapper release(BytesWrapper bw) {
        bw.decrRef();
        memory -= bw.memoryBytes();
        return bw;
    }

    public boolean add(BytesWrapper value) {
        ensureWritable();
        if (set.contains(value)) return false;
        retain(value);
        set.add(value);
        return true;
    }

    public boolean remove(BytesWrapper value) {
        ensureWritable();
        if (!set.contains(value)) return false;
        release(value);
        set.remove(value);
        return true;
    }

    public boolean contains(BytesWrapper value) {
        return set.contains(value);
    }

    public HashSet<BytesWrapper> members() {
        return set;
    }

    public int size() {
        return set.size();
    }

    @Override
    public Type type() {
        return Type.SET;
    }

    @Override
    protected void free() {
        for (BytesWrapper bw : set) {
            release(bw);
        }
        set.clear();
        memory = 0;
    }

    @Override
    public RedisObject deepCopy() {
        HashSet<BytesWrapper> copy = new HashSet<>();
        long mem = 0;
        for (BytesWrapper bw : set) {
            bw.incrRef();
            copy.add(bw);
            mem += bw.memoryBytes();
        }
        return new RedisSet(copy, mem);
    }

    @Override
    public long memoryBytes() {
        return memory;
    }
}
