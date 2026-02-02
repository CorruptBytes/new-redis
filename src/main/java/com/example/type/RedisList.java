package com.example.type;


import java.util.LinkedList;
import java.util.List;

public class RedisList extends RedisObject {

    private final LinkedList<BytesWrapper> list;
    private long memory;

    public RedisList() {
        this.list = new LinkedList<>();
        this.memory = 0;
    }

    public RedisList(LinkedList<BytesWrapper> source, long memory) {
        this.list = source;
        this.memory = memory;
    }

    /* ================= 基本操作 ================= */

    public int size() {
        return list.size();
    }

    public void lpush(BytesWrapper value) {
        list.addFirst(retain(value));
    }

    public void rpush(BytesWrapper value) {
        list.addLast(retain(value));
    }

    public BytesWrapper lpop() {
        return list.isEmpty() ? null : release(list.removeFirst());
    }

    public BytesWrapper rpop() {
        return list.isEmpty() ? null : release(list.removeLast());
    }

    public BytesWrapper index(int idx) {
        if (idx < 0) idx = list.size() + idx;
        return (idx < 0 || idx >= list.size()) ? null : list.get(idx);
    }

    public List<BytesWrapper> range(int start, int end) {
        int size = list.size();
        if (start < 0) start += size;
        if (end < 0) end += size;
        start = Math.max(0, start);
        end = Math.min(size - 1, end);

        if (start > end) return List.of();

        return list.subList(start, end + 1);
    }

    /* ================= RedisObject ================= */

    @Override
    public Type type() {
        return Type.LIST;
    }

    @Override
    protected void free() {
        for (BytesWrapper bw : list) {
            release(bw);
        }
        list.clear();
        memory = 0;
    }

    @Override
    public RedisObject deepCopy() {
        LinkedList<BytesWrapper> copy = new LinkedList<>();
        long mem = 0;

        for (BytesWrapper bw : list) {
            bw.incrRef();
            copy.add(bw);
            mem += bw.memoryBytes();
        }

        return new RedisList(copy, mem);
    }

    @Override
    public long memoryBytes() {
        return memory;
    }

    /* ================= 内存管理 ================= */

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
}

