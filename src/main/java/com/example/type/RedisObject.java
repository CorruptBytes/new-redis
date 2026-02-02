package com.example.type;

public abstract class RedisObject implements MemoryTrackable{
    public enum Type {
        STRING, HASH, LIST, SET, ZSET
    }

    public abstract Type type();

    private int refCount = 1;
    private boolean cowMarked = false;

    public void incrRef() {
        refCount++;
    }

    public void decrRef() {
        if (--refCount == 0) {
            free();
        }
    }

    public boolean isShared() {
        return refCount > 1 || cowMarked;
    }

    public void markCow() {
        cowMarked = true;
    }
    public void clearCow() {
        cowMarked = false;
    }

    protected abstract void free();
    public abstract RedisObject deepCopy();

    /**
     * 返回对象的大小（字节）
     */
    @Override
    public abstract long memoryBytes();

    /**
     * 本次写操作预计增加的内存（默认 0）
     * LPUSH / HSET / ZADD 覆盖它
     */
    public long estimateWriteDelta()
    {
        return 0;
    }
}
