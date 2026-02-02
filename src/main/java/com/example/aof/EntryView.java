package com.example.aof;

public interface EntryView<K,V> {
    K key();
    V value();
    Long expireAt(); // nullable
}