package com.example.database;

import com.example.type.RedisObject;

public interface BlockedClient<K> {
    K key();                        // 阻塞在哪个 key
    void unblock(RedisObject val);  // 被唤醒时回调
    void timeout();                 // 超时回调
}
