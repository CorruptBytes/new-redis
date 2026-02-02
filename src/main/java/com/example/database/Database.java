package com.example.database;

import com.example.aof.EntryView;
import com.example.aof.SnapshotEntry;
import com.example.type.RedisObject;
import com.example.type.RedisString;
import com.example.util.PropertiesUtil;
import com.example.util.GlobPattern;
import java.util.*;
import java.util.regex.Pattern;

public class Database<K, V> {

    private static final int LOOKUPS_PER_LOOP = 20;
    private static final int TIME_LIMIT_MS = 25;
    private static final int EXPIRE_PERCENT_THRESHOLD = 25;

    private long maxMemory = Long.MAX_VALUE;
    private EvictionPolicy evictionPolicy = EvictionPolicy.NOEVICTION;
    // 简化版内存统计（你后面可以换成精确估算）
    private long usedMemory = 0;
    private DictTable<K, V>[] ht = new DictTable[2];
    private DictTable<K, Long>[] expires = new DictTable[2];
    private final Map<K, Deque<BlockedClient<K>>> blocked = new HashMap<>();

    //-1表示没有在进行渐进式hash
    private int rehashIdx = -1;
    private final Random random = new Random();

    private void ensureCanWrite(long delta) {
        if (usedMemory + delta <= maxMemory) return;
        tryEvictIfNeeded(delta);
    }

    private void accountMemory(long delta) {
        usedMemory += delta;
    }
    public Database(int initCapacity) {
        ht[0] = new DictTable<>(initCapacity);
        expires[0] = new DictTable<>(initCapacity);
        // 从配置文件读取最大内存
        long configuredMaxMemory = PropertiesUtil.getMaxMemory();
        // 当配置文件中没有配置 maxmemory 或者配置为负数时，表示无内存上限
        this.maxMemory = configuredMaxMemory <= 0 ? Long.MAX_VALUE : configuredMaxMemory;
    }
    private boolean overMaxMemory() {
        return usedMemory >= maxMemory;
    }

    @SuppressWarnings("unchecked")
    public RedisObject writeValue(K key) {
        RedisObject obj = (RedisObject) get(key);
        if (obj == null) return null;

        // 估算写入增量
        long delta = obj.estimateWriteDelta();

        // 如果被 COW，需要加上 deepCopy 的内存
        if (obj.isShared()) {
            RedisObject copy = obj.deepCopy();
            delta += copy.memoryBytes();
        }

        ensureCanWrite(delta);

        RedisObject writable = prepareWrite(obj);
        if (writable != obj) {
            put(key, (V) writable);
        }

        accountMemory(delta);
        return writable;
    }



    private boolean isRehashing() {
        return rehashIdx != -1;
    }

    private void startRehash(int newCap) {
        ht[1] = new DictTable<>(newCap);
        expires[1] = new DictTable<>(newCap);
        rehashIdx = 0;
    }


    private void rehashStep(int steps) {
        if (!isRehashing()) return;

        while (steps-- > 0 && rehashIdx < ht[0].table.length) {

            // ===== 1. 迁移 data 表 =====
            Entry<K, V> e = ht[0].table[rehashIdx];
            ht[0].table[rehashIdx] = null;

            while (e != null) {
                Entry<K, V> next = e.next;
                int idx = hash(e.key) & ht[1].mask;
                e.next = ht[1].table[idx];
                ht[1].table[idx] = e;
                ht[1].size++;
                ht[0].size--;
                e = next;
            }

            // ===== 2. 迁移 expires 表 =====
            Entry<K, Long> ex = expires[0].table[rehashIdx];
            expires[0].table[rehashIdx] = null;

            while (ex != null) {
                Entry<K, Long> next = ex.next;
                int idx = hash(ex.key) & expires[1].mask;
                ex.next = expires[1].table[idx];
                expires[1].table[idx] = ex;
                expires[1].size++;
                expires[0].size--;
                ex = next;
            }

            rehashIdx++;
        }

        // ===== 3. rehash 结束 =====
        if (ht[0].size == 0) {
            ht[0] = ht[1];
            ht[1] = null;

            expires[0] = expires[1];
            expires[1] = null;

            rehashIdx = -1;
        }
    }



    public void put(K key, V value) {
        rehashStep(1);
        if (!isRehashing() && ht[0].size >= ht[0].table.length) {
            startRehash(ht[0].table.length * 2);
        }

        // 精确计算内存差值
        long delta = 0;
        V old = get(key);
        if (old instanceof RedisObject oldObj) {
            delta -= oldObj.memoryBytes(); // 减去旧对象内存
        }
        if (value instanceof RedisObject newObj) {
            delta += newObj.memoryBytes(); // 加上新对象内存
        }

        ensureCanWrite(delta); // 写前检查内存
        insert(key, value, isRehashing() ? ht[1] : ht[0]);
        accountMemory(delta);
    }


    private void insert(K key, V value, DictTable<K, V> table) {
        int idx = hash(key) & table.mask;
        Entry<K, V> e = table.table[idx];

        while (e != null) {
            if (Objects.equals(e.key, key)) {
                e.value = value;
                return;
            }
            e = e.next;
        }

        table.table[idx] = new Entry<>(key, value, table.table[idx]);
        table.size++;
    }

    public V get(K key) {
        rehashStep(1);

        if (isExpired(key)) {
            deleteExpiredKey(key);
            return null;
        }

        V v = find(key, ht[0]);
        return isRehashing() && v == null ? find(key, ht[1]) : v;
    }


    private V find(K key, DictTable<K, V> table) {
        if (table == null) return null;
        int idx = hash(key) & table.mask;
        Entry<K, V> e = table.table[idx];
        while (e != null) {
            if (Objects.equals(e.key, key)) return e.value;
            e = e.next;
        }
        return null;
    }

    public boolean remove(K key) {
        rehashStep(1);

        V obj = get(key);
        long delta = 0;
        if (obj instanceof RedisObject ro) {
            delta -= ro.memoryBytes(); // 删除对象减少内存
        }

        boolean removed = delete(key, ht[0]) || (isRehashing() && delete(key, ht[1]));

        if (removed) {
            accountMemory(delta);
            deleteExpire(key, expires[0]);
            if (isRehashing()) {
                deleteExpire(key, expires[1]);
            }
        }
        return removed;
    }




    private boolean delete(K key, DictTable<K, V> table) {
        if (table == null) return false;
        int idx = hash(key) & table.mask;
        Entry<K, V> prev = null, cur = table.table[idx];

        while (cur != null) {
            if (Objects.equals(cur.key, key)) {
                if (prev == null) table.table[idx] = cur.next;
                else prev.next = cur.next;
                table.size--;
                return true;
            }
            prev = cur;
            cur = cur.next;
        }
        return false;
    }

    public void expire(K key, long ttlMillis) {
        long expireAt = System.currentTimeMillis() + ttlMillis;
        insertExpire(key, expireAt, isRehashing() ? expires[1] : expires[0]);
    }
    private void insertExpire(K key, long expireAt, DictTable<K, Long> table) {
        int idx = hash(key) & table.mask;
        Entry<K, Long> e = table.table[idx];

        while (e != null) {
            if (Objects.equals(e.key, key)) {
                e.value = expireAt;
                return;
            }
            e = e.next;
        }

        table.table[idx] = new Entry<>(key, expireAt, table.table[idx]);
        table.size++;
    }

    private boolean isExpired(K key) {
        Long t = findExpire(key, expires[0]);
        if (t == null && isRehashing()) {
            t = findExpire(key, expires[1]);
        }
        return t != null && System.currentTimeMillis() >= t;
    }
    private Long findExpire(K key, DictTable<K, Long> table) {
        if (table == null) return null;
        int idx = hash(key) & table.mask;
        Entry<K, Long> e = table.table[idx];
        while (e != null) {
            if (Objects.equals(e.key, key)) return e.value;
            e = e.next;
        }
        return null;
    }
    public Long getExpireTime(K key) {
        rehashStep(1);

        Long t = findExpire(key, expires[0]);
        if (t == null && isRehashing()) {
            t = findExpire(key, expires[1]);
        }
        return t;
    }
    private void deleteExpiredKey(K key) {
        // 删除 data
        boolean removed = remove(key);

        // 删除 expires
        deleteExpire(key, expires[0]);
        if (isRehashing()) {
            deleteExpire(key, expires[1]);
        }
    }
    private boolean deleteExpire(K key, DictTable<K, Long> table) {
        if (table == null) return false;
        int idx = hash(key) & table.mask;
        Entry<K, Long> prev = null, cur = table.table[idx];

        while (cur != null) {
            if (Objects.equals(cur.key, key)) {
                if (prev == null) table.table[idx] = cur.next;
                else prev.next = cur.next;
                table.size--;
                return true;
            }
            prev = cur;
            cur = cur.next;
        }
        return false;
    }


    public K randomKey() {
        rehashStep(1);

        DictTable<K, V> table = isRehashing() && random.nextBoolean()
                ? ht[1]
                : ht[0];

        if (table == null || table.size == 0) return null;

        while (true) {
            int idx = random.nextInt(table.table.length);
            Entry<K, V> e = table.table[idx];
            if (e == null) continue;

            if (isExpired(e.key)) {
                deleteExpiredKey(e.key);
                continue;
            }

            int steps = random.nextInt(4);
            while (steps-- > 0 && e.next != null) {
                e = e.next;
            }
            return e.key;
        }
    }
    public void activeExpireCycle() {
        long startTime = System.currentTimeMillis();
        boolean repeat = true;

        while (repeat) {
            repeat = false;

            int expired = 0;
            int sampled = 0;

            for (int i = 0; i < LOOKUPS_PER_LOOP; i++) {

                // 1. 没有设置过期的 key
                if (expires[0] == null || expires[0].size == 0) {
                    return;
                }

                // 2. 时间片耗尽，立刻停止
                if (System.currentTimeMillis() - startTime > TIME_LIMIT_MS) {
                    return;
                }

                // 3. 随机从 expires 中取 key
                K key = randomExpireKey();
                if (key == null) {
                    return;
                }

                sampled++;

                // 4. 过期即删
                if (isExpired(key)) {
                    deleteExpiredKey(key);
                    expired++;
                }

                // 5. 推进 rehash
                rehashStep(1);
            }

            // 6. 删除比例驱动是否继续
            if (sampled > 0) {
                int expiredPercent = (expired * 100) / sampled;
                if (expiredPercent > EXPIRE_PERCENT_THRESHOLD) {
                    repeat = true;
                }
            }
        }
    }


    private K randomExpireKey() {
        DictTable<K, Long> table =
                isRehashing() && random.nextBoolean()
                        ? expires[1]
                        : expires[0];

        if (table == null || table.size == 0) return null;

        while (true) {
            int idx = random.nextInt(table.table.length);
            Entry<K, Long> e = table.table[idx];
            if (e == null) continue;

            int steps = random.nextInt(4);
            while (steps-- > 0 && e.next != null) {
                e = e.next;
            }
            return e.key;
        }
    }


    public int size() {
        return (ht[0] == null ? 0 : ht[0].size)
                + (ht[1] == null ? 0 : ht[1].size);
    }

    private int hash(Object key) {
        if (key == null) throw new RuntimeException("不支持NULL键");
        return key.hashCode();
    }
    //COW
    public void markAllObjectsCow() {
        markTableCow(ht[0]);
        if (isRehashing()) markTableCow(ht[1]);
    }

    private void markTableCow(DictTable<K,V> table) {
        if (table == null) return;
        for (Entry<K,V> e : table.table) {
            while (e != null) {
                if (e.value instanceof RedisObject ro) {
                    ro.markCow();
                }
                e = e.next;
            }
        }
    }
    public List<EntryView<K,V>> entriesSnapshot() {
        rehashStep(1);

        List<EntryView<K,V>> list = new ArrayList<>();
        collectEntries(ht[0], expires[0], list);
        if (isRehashing()) {
            collectEntries(ht[1], expires[1], list);
        }
        return list;
    }

    private void collectEntries(
            DictTable<K,V> data,
            DictTable<K,Long> exp,
            List<EntryView<K,V>> out) {

        if (data == null) return;

        for (Entry<K,V> e : data.table) {
            while (e != null) {
                K key = e.key;
                if (!isExpired(key)) {
                    Long t = exp == null ? null : findExpire(key, exp);
                    out.add(new SnapshotEntry<>(key, e.value, t));
                }
                e = e.next;
            }
        }
    }
    public RedisObject prepareWrite(RedisObject obj) {
        if (obj == null) return null;

        // 没有被 COW 标记，直接写
        if (!obj.isShared()) {
            return obj;
        }

        // ⚠️ 被 rewrite 引用，必须复制
        RedisObject copy = obj.deepCopy();

        // 新对象属于主线程
        copy.clearCow();

        return copy;
    }

    // 注册阻塞客户端
    public void block(K key, BlockedClient<K> client) {
        blocked.computeIfAbsent(key, k -> new ArrayDeque<>()).addLast(client);
    }

    // 写入数据后尝试唤醒阻塞客户端
    public void putAndSignal(K key, V value) {
        put(key, value);

        Deque<BlockedClient<K>> q = blocked.get(key);
        if (q != null && !q.isEmpty()) {
            BlockedClient<K> client = q.pollFirst();
            if (client != null && value instanceof RedisObject ro) {
                client.unblock(ro);
            }
            if (q.isEmpty()) blocked.remove(key);
        }
    }

    // 超时唤醒
    public void unblockTimeout(K key, BlockedClient<K> client) {
        Deque<BlockedClient<K>> q = blocked.get(key);
        if (q != null && q.remove(client) && q.isEmpty()) {
            blocked.remove(key);
        }
        client.timeout();
    }
    public List<K> keys(String pattern) {
        rehashStep(1);

        List<K> result = new ArrayList<>();

        // 1. 编译 glob pattern
        Pattern regex = GlobPattern.compile(pattern);

        // 2. 使用快照遍历（Redis 风格）
        for (EntryView<K, V> ev : entriesSnapshot()) {
            K key = ev.key();
            if (key == null) continue;

            if (regex.matcher(key.toString()).matches()) {
                result.add(key);
            }
        }
        return result;
    }

    private void tryEvictIfNeeded(long delta) {
        if (usedMemory + delta <= maxMemory) {
            return;
        }

        switch (evictionPolicy) {
            case NOEVICTION -> throw new RuntimeException(
                    "OOM command not allowed when used memory > 'maxmemory'");

            case ALLKEYS_RANDOM -> evictAllKeysRandom(delta);

            case VOLATILE_RANDOM -> evictVolatileRandom(delta);
        }
    }
    private void evictVolatileRandom(long delta) {
        while (usedMemory + delta > maxMemory) {
            K key = randomExpireKey();
            if (key == null) {
                throw new RuntimeException("OOM no volatile key to evict");
            }
            deleteExpiredKey(key);
        }
    }
    private void evictAllKeysRandom(long delta) {
        while (usedMemory + delta > maxMemory) {
            K key = randomKey();
            if (key == null) {
                throw new RuntimeException("OOM no key to evict");
            }
            deleteExpiredKey(key);
        }
    }

    /**
     * 清空数据库中的所有数据
     */
    public void clear() {
        // 重置哈希表
        ht[0] = new DictTable<>(1024);
        ht[1] = null;
        
        // 重置过期表
        expires[0] = new DictTable<>(1024);
        expires[1] = null;
        
        // 重置阻塞客户端
        blocked.clear();
        
        // 重置内存使用
        usedMemory = 0;
        
        // 重置 rehash 索引
        rehashIdx = -1;
        
        System.out.println("Database cleared");
    }

}

