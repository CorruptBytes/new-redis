package com.example.command;

import com.example.command.general.*;
import com.example.command.list.*;
import com.example.command.set.SCARD;
import com.example.command.set.SREM;
import com.example.command.string.*;
import com.example.command.zset.ZCARD;
import com.example.command.zset.ZREM;
import com.example.command.zset.ZREVRANGE;
import lombok.Getter;

import java.util.function.Supplier;

@Getter
public enum CommandType {
    /**
     * 通用命令
     */
    PING(false, PING::new),
    ECHO(false, ECHO::new),
    DEL(true, DEL::new),
    EXISTS(false, EXISTS::new),
    RANDOMKEY(false, RANDOMKEY::new),
    EXPIRE(false, EXPIRE::new),
    TTL(false, TTL::new),
    BGREWRITEAOF(false, BGREWRITEAOF::new),
    KEYS(false, KEYS::new),
    SAVE(false, SAVE::new),
    BGSAVE(false,BGSAVE::new ),
    SLAVEOF(false, SLAVEOF::new),
    SYNC(false, com.example.command.repl.SYNC::new),
    INFO(false, INFO::new),

    /**
     * 字符串命令
     */
    SET(true, com.example.command.string.SET::new),
    GET(false, com.example.command.string.GET::new),
    MSET(true, com.example.command.string.MSET::new),
    MGET(false, com.example.command.string.MGET::new),
    SETNX(true, com.example.command.string.SETNX::new),
    SETEX(true,SETEX::new),
    INCR(true, com.example.command.string.INCR::new),
    /**
     * 列表命令
     */
    LPUSH(true, LPush::new),
    RPUSH(true, RPush::new),
    LPOP(true, LPop::new),
    LRANGE(false, LRange::new),
    BLPOP(false, com.example.command.list.BLPOP::new),
    /**
     * SET命令
     */
    SADD(true, com.example.command.set.SADD::new),
    SMEMBERS(false, com.example.command.set.SMEMBERS::new),
    SREM(true, com.example.command.set.SREM::new),
    SCARD(false, com.example.command.set.SCARD::new),
    /**
     * HASH命令
     */
    HSET(true, com.example.command.hash.HSET::new),
    HGET(false, com.example.command.hash.HGET::new ),
    HMSET(true, com.example.command.hash.HMSET::new),
    HMGET(false, com.example.command.hash.HMGET::new),
    /**
     * ZSET命令
     */
    ZADD(true, com.example.command.zset.ZADD::new ), ZRANGE(false, com.example.command.zset.ZRANGE::new),
    ZREM(true, com.example.command.zset.ZREM::new ),
    ZCARD(false, com.example.command.zset.ZCARD::new),
    ZREVRANGE(false, com.example.command.zset.ZREVRANGE::new);

    final boolean write;
    final Supplier<Command> supplier;
    CommandType(boolean write,Supplier<Command> supplier) {
        this.write = write;
        this.supplier = supplier;
    }

}
