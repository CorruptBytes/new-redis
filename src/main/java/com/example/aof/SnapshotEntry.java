package com.example.aof;

public record SnapshotEntry<K,V>(K key, V value, Long expireAt)
        implements EntryView<K,V> {}
