package com.example.type;

import lombok.Getter;

import java.util.*;
import java.util.stream.Collectors;

public class RedisZSet extends RedisObject {

    private TreeMap<ZsetKey, Long> map;
    private long memory;

    public RedisZSet() {
        map = new TreeMap<>(Comparator.<ZsetKey>comparingLong(o -> o.score)
                .thenComparing(o -> o.key));
        memory = 0;
    }

    public RedisZSet(TreeMap<ZsetKey, Long> source, long memory) {
        this.map = source;
        this.memory = memory;
    }

    private void ensureWritable() {
        if (isShared()) {
            map = new TreeMap<>(map);
            clearCow();
        }
    }
    public int size() {
        return map.size();
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

    public int add(List<ZsetKey> keys) {
        ensureWritable();
        int added = 0;
        for (ZsetKey newKey : keys) {
            ZsetKey existing = findByKey(newKey.key);
            if (existing != null) {
                map.remove(existing);
                existing.score = newKey.score;
                map.put(existing, existing.score);
            } else {
                map.put(new ZsetKey(retain(newKey.key), newKey.score), newKey.score);
                added++;
            }
        }
        return added;
    }

    public int remove(List<BytesWrapper> members) {
        ensureWritable();
        int removed = 0;
        for (BytesWrapper member : members) {
            ZsetKey key = findByKey(member);
            if (key != null) {
                map.remove(key);
                release(key.key);
                removed++;
            }
        }
        return removed;
    }

    public List<ZsetKey> range(int start, int end) {
        return map.keySet().stream()
                .skip(start)
                .limit(Math.max(end - start + 1, 0))
                .collect(Collectors.toList());
    }

    /**
     * 倒序范围
     */
    public List<ZsetKey> reRange(int start, int end) {
        return map.descendingKeySet().stream()
                .skip(start)
                .limit(Math.max(end - start + 1, 0))
                .toList();
    }
    private ZsetKey findByKey(BytesWrapper key) {
        for (ZsetKey k : map.keySet()) {
            if (k.key.equals(key)) return k;
        }
        return null;
    }

    @Override
    public Type type() {
        return Type.ZSET;
    }

    @Override
    protected void free() {
        for (ZsetKey k : map.keySet()) release(k.key);
        map.clear();
        memory = 0;
    }

    @Override
    public RedisObject deepCopy() {
        TreeMap<ZsetKey, Long> copy = new TreeMap<>(map);
        long mem = 0;
        for (ZsetKey k : copy.keySet()) k.key.incrRef();
        for (ZsetKey k : copy.keySet()) mem += k.key.memoryBytes();
        return new RedisZSet(copy, mem);
    }

    @Override
    public long memoryBytes() {
        return memory;
    }

    public static class ZsetKey implements Comparable<ZsetKey> {
        @Getter
        final BytesWrapper key;
        long score;

        public ZsetKey(BytesWrapper key, long score) {
            this.key = key;
            this.score = score;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ZsetKey)) return false;
            return key.equals(((ZsetKey) o).key);
        }

        @Override
        public int hashCode() {
            return key.hashCode();
        }

        @Override
        public int compareTo(ZsetKey o) {
            int cmp = Long.compare(this.score, o.score);
            if (cmp != 0) return cmp;
            return this.key.compareTo(o.key);
        }

        public long getScore() {
            return score;
        }
    }
}
