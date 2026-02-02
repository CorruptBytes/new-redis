package com.example.evnetloop;

import com.example.ServerContext;
import com.example.aof.AofManager;
import com.example.command.AbstractCommand;
import com.example.command.Command;
import com.example.command.result.BlockedResult;
import com.example.database.BlockingListClient;
import com.example.database.Database;
import com.example.handler.ClientConnection;
import com.example.resp.Errors;
import com.example.resp.RespArray;
import com.example.command.result.CommandResult;
import com.example.command.result.NoReplyResult;
import com.example.type.RedisObject;
import com.example.type.RedisString;
import com.example.util.NamedThreadFactory;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultEventLoopGroup;
import io.netty.channel.EventLoop;
import io.netty.channel.EventLoopGroup;
import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.TimeUnit;

public final class MainEventLoop {

    @Getter
    private final EventLoopGroup group;
    private final EventLoop eventLoop;
    @Setter
    private ServerContext serverContext;
    
    public MainEventLoop(ServerContext server) {
        this.serverContext = server;
        this.group = new DefaultEventLoopGroup(1, new NamedThreadFactory("redis_main_"));
        this.eventLoop = group.next();
    }

    public void start() {
        // 注册 Redis 内置时间事件（serverCron）
        registerTimeEvents();
    }

    private void registerTimeEvents() {
        // 等价于 Redis 的 serverCron，使用更合理的调度间隔
        eventLoop.scheduleAtFixedRate(() -> {
            try {
                serverContext.db().activeExpireCycle();
                serverContext.aof().flush();
                // 未来：AOF / RDB / rehash / stats
            } catch (Throwable t) {
                // Redis 风格：异常不能打断主循环
            }
        }, 100, 200, TimeUnit.MILLISECONDS); // 增加调度间隔，减少CPU占用
    }

    public void submit(Command command, ChannelHandlerContext ctx) {
        eventLoop.execute(() -> {
            try {
                // 从节点拒绝写命令
                if (serverContext.getRole() == ServerContext.ServerRole.SLAVE && command.type().isWrite()) {
                    ctx.writeAndFlush(new Errors("READONLY You can't write against a read only replica"));
                    return;
                }
                
                CommandResult result = command.execute(serverContext);
                if (command.type().isWrite()) {
                    // 复用RespArray对象，减少对象创建
                    RespArray raw = new RespArray(((AbstractCommand) command).argv);
                    serverContext.aof().append(raw);
                    
                    // 传播命令到从服务器
                    serverContext.getReplicaManager().propagateCommand(command, ctx);
                }
                // 处理阻塞命令
                if (result instanceof BlockedResult blocked) {
                    Database<RedisString, RedisObject> db = serverContext.db();
                    ClientConnection connection = new ClientConnection(ctx);
                    BlockingListClient client = 
                            new BlockingListClient(blocked.getKey(), blocked.isLeft(), connection::reply, serverContext);
                    db.block(blocked.getKey(), client);
                    serverContext.main().schedule(() -> {
                        db.unblockTimeout(blocked.getKey(), client);
                    }, blocked.getTimeoutMillis(), TimeUnit.MILLISECONDS);
                    return;
                }
                if (ctx != null && !(result instanceof NoReplyResult)) {
                    ctx.writeAndFlush(result.toResp());
                }
            } catch (Throwable t) {
                if (ctx != null) {
                    ctx.writeAndFlush(new Errors(t.getMessage()));
                }
            }
        });
    }

    public void shutdown() {
        group.shutdownGracefully();
    }
}

