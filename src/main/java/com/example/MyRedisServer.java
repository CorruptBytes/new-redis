package com.example;

import com.example.aof.AofManager;
import com.example.channel.ChannelSelectStrategy;
import com.example.channel.DefaultChannelSelectStrategy;
import com.example.channel.LocalChannelOption;
import com.example.database.Database;
import com.example.evnetloop.MainEventLoop;
import com.example.handler.CommandDecoder;
import com.example.handler.CommandDispatchHandler;
import com.example.handler.ConnectionHandler;
import com.example.handler.RespEncoderHandler;
import com.example.rdb.DefaultRdbModule;
import com.example.type.RedisObject;
import com.example.type.RedisString;
import com.example.util.PropertiesUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.ServerSocketChannel;

import java.nio.file.Path;
import java.nio.file.Paths;


public final class MyRedisServer implements RedisServer {

    private final ServerContext context;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel serverChannel;


    public MyRedisServer(Database<RedisString, RedisObject> database) {
        // 从配置文件读取 AOF 路径
        String aofPath = PropertiesUtil.getAofPath() + "redis.aof";
        AofManager aofManager = new AofManager(aofPath, database);
        MainEventLoop mainEventLoop = new MainEventLoop(null);
        this.context = new ServerContext(database,aofManager,mainEventLoop);
        mainEventLoop.setServerContext(context);
        context.setRdbModule(new DefaultRdbModule());
    }

    @Override
    public void start() {
        // 1️⃣ 启动时加载 RDB
        try {
            Path rdbFile = Paths.get(PropertiesUtil.getRdbFile());
            context.getRdbModule().load(context.db(), rdbFile);
            System.out.println("RDB loaded from " + rdbFile.toAbsolutePath());
        } catch (Exception e) {
            System.err.println("Failed to load RDB: " + e.getMessage());
        }
        
        // 加载 AOF（如果启用）
        if (PropertiesUtil.getAppendOnly()) {
            try {
                context.aof().load(context.db());
                System.out.println("AOF loaded successfully");
            } catch (Exception e) {
                System.err.println("Failed to load AOF: " + e.getMessage());
            }
        }
        
        // 开启 Redis 主事件循环
        this.context.mainEventLoop().start();
        
        // 检查是否配置了 slaveof，如果配置了就启动复制流程
        if (PropertiesUtil.isSlaveofConfigured()) {
            String masterHost = PropertiesUtil.getMasterHostAddress();
            int masterPort = PropertiesUtil.getMasterPort();
            System.out.println("Configured as slave of " + masterHost + ":" + masterPort);
            context.getReplicaManager().handleSlaveof(masterHost, masterPort);
        }

        //  选择平台最优 IO 模型
        ChannelSelectStrategy strategy = new DefaultChannelSelectStrategy();
        LocalChannelOption<ServerSocketChannel> option = strategy.select();

        bossGroup = option.boss();
        workerGroup = option.selectors();

        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
                .channel(option.getChannelClass())
                .childHandler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) {
                        ChannelPipeline p = ch.pipeline();
                        p.addLast(new CommandDecoder());
                        p.addLast(new RespEncoderHandler());
                        p.addLast(new ConnectionHandler(context));
                        p.addLast(new CommandDispatchHandler(context.mainEventLoop()));
                    }
                })
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.SO_KEEPALIVE, PropertiesUtil.getTcpKeepAlive());

        try {
            int serverPort = PropertiesUtil.getNodePort();
            serverChannel = bootstrap.bind(serverPort).sync().channel();
            System.out.println("Redis server started on port " + serverPort);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void close() {

        if (serverChannel != null) {
            serverChannel.close();
        }

        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }

        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }

        if (this.context.mainEventLoop() != null) {
            this.context.mainEventLoop().shutdown();
        }
    }

    @Override
    public Database<RedisString, RedisObject> getDatabase() {
        return context.db();
    }

    public static void main(String[] args) {
        // 直接从配置文件读取端口，不再需要命令行参数
        System.out.println("Starting MyRedis server...");
        System.out.println("Port: " + PropertiesUtil.getNodePort());
        System.out.println("AOF enabled: " + PropertiesUtil.getAppendOnly());
        System.out.println("RDB file: " + PropertiesUtil.getRdbFile());
        System.out.println("Max memory: " + PropertiesUtil.getMaxMemory() + " bytes");
        
        new MyRedisServer(new Database<>(1024)).start();
    }
}

