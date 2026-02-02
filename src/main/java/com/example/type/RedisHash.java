package com.example.type;



import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class RedisHash extends RedisObject {

    private Map<BytesWrapper, BytesWrapper> map;
    private long memory;

    public RedisHash() {
        this.map = new HashMap<>();
        this.memory = 0;
    }

    public RedisHash(Map<BytesWrapper, BytesWrapper> source, long memory) {
        this.map = source;
        this.memory = memory;
    }

    private void ensureWritable() {
        if (isShared()) {
            map = new HashMap<>(map);
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

    public void put(BytesWrapper key, BytesWrapper value) {
        ensureWritable();
        BytesWrapper old = map.put(key, retain(value));
        if (old != null) release(old);
    }

    public BytesWrapper get(BytesWrapper key) {
        return map.get(key);
    }

    public void remove(BytesWrapper key) {
        ensureWritable();
        BytesWrapper old = map.remove(key);
        if (old != null) release(old);
    }

    public int size() {
        return map.size();
    }

    public Set<Map.Entry<BytesWrapper, BytesWrapper>> entries() {
        return map.entrySet();
    }

    @Override
    public Type type() {
        return Type.HASH;
    }

    @Override
    protected void free() {
        for (BytesWrapper v : map.values()) release(v);
        map.clear();
        memory = 0;
    }

    @Override
    public RedisObject deepCopy() {
        Map<BytesWrapper, BytesWrapper> copy = new HashMap<>();
        long mem = 0;
        for (Map.Entry<BytesWrapper, BytesWrapper> e : map.entrySet()) {
            e.getKey().incrRef();
            e.getValue().incrRef();
            copy.put(e.getKey(), e.getValue());
            mem += e.getKey().memoryBytes() + e.getValue().memoryBytes();
        }
        return new RedisHash(copy, mem);
    }

    @Override
    public long memoryBytes() {
        return memory;
    }
}
