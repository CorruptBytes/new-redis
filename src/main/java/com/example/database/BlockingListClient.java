package com.example.database;

import com.example.ServerContext;
import com.example.command.CommandFactory;
import com.example.command.result.ArrayResult;
import com.example.command.result.BulkStringResult;
import com.example.command.result.CommandResult;
import com.example.resp.BulkString;
import com.example.resp.Resp;
import com.example.resp.RespArray;
import com.example.type.BytesWrapper;
import com.example.type.RedisList;
import com.example.type.RedisObject;
import com.example.type.RedisString;

public class BlockingListClient implements BlockedClient<RedisString> {

    private final RedisString key;
    private final boolean left; // true = LPOP, false = RPOP
    private final java.util.function.Consumer<CommandResult> responder;
    private final ServerContext serverContext;
    public BlockingListClient(RedisString key, boolean left, java.util.function.Consumer<CommandResult> responder,ServerContext serverContext) {
        this.key = key;
        this.left = left;
        this.responder = responder;
        this.serverContext = serverContext;
    }

    @Override
    public RedisString key() { return key; }

    @Override
    public void unblock(RedisObject value) {
        if (!(value instanceof RedisList list) || list.size() == 0) {
            responder.accept(BulkStringResult.NULL);
            return;
        }

        BytesWrapper bw = left ? list.lpop() : list.rpop();
        // 写回数据库
        serverContext.db().put(key, list);

        // ✅ 持久化到 AOF
        // 构造对应的 LPOP 或 RPOP 命令
        RespArray cmd = new RespArray(new Resp[]{
                new BulkString(left ? "LPOP".getBytes() : "RPOP".getBytes()),
                new BulkString(key.getValue().getByteArray())
        });
        serverContext.aof().append(cmd);
        responder.accept(new ArrayResult(new CommandResult[]{
            new BulkStringResult(key.getValue().getByteArray()),
            new BulkStringResult(bw.getByteArray())
        }));
    }

    @Override
    public void timeout() {
        responder.accept(BulkStringResult.NULL);
    }
}
