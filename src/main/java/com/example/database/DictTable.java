package com.example.database;

public class DictTable<K,V> {
    public Entry<K, V>[] table;
    public int size;
    public int mask;

    @SuppressWarnings("unchecked")
    public DictTable(int capacity) {
        capacity = tableSizeFor(capacity);
        table = (Entry<K, V>[]) new Entry[capacity];
        mask = capacity - 1;
    }
    public static int tableSizeFor(int cap) {
        //处理 cap 本身就是 2 的幂的情况
        int n = cap - 1;
        //把最高位的 1 后面的所有位都变成 1
        n |= n >>> 1;
        n |= n >>> 2;
        n |= n >>> 4;
        n |= n >>> 8;
        n |= n >>> 16;
        return n > 0 ? n + 1 : 1;
    }

}
