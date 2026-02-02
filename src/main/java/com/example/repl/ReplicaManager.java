package com.example.repl;

import com.example.ServerContext;
import com.example.command.Command;
import com.example.resp.RespArray;
import com.example.command.AbstractCommand;
import io.netty.channel.ChannelHandlerContext;
import java.util.ArrayList;
import java.util.List;

public class ReplicaManager {
    private final ServerContext serverContext;
    private ReplicaState replicaState;
    private List<ChannelHandlerContext> slaveConnections;
    
    public ReplicaManager(ServerContext serverContext) {
        this.serverContext = serverContext;
        this.replicaState = ReplicaState.DISCONNECTED;
        this.slaveConnections = new ArrayList<>();
    }
    
    public void handleSlaveof(String masterHost, int masterPort) {
        // 设置从服务器角色
        serverContext.setRole(ServerContext.ServerRole.SLAVE);
        serverContext.setMasterHost(masterHost);
        serverContext.setMasterPort(masterPort);
        
        // 启动复制过程
        startReplication();
    }
    
    private void startReplication() {
        // 1. 建立与主服务器的连接
        // 2. 发送SYNC命令
        // 3. 接收并处理RDB文件
        // 4. 进入命令传播阶段
        System.out.println("Starting replication from " + serverContext.getMasterHost() + ":" + serverContext.getMasterPort());
        
        // 更新复制状态
        replicaState = ReplicaState.CONNECTING;
        
        // 实现简单的复制逻辑：建立与主服务器的连接
        try {
            // 使用Netty建立与主服务器的连接
            io.netty.bootstrap.Bootstrap bootstrap = new io.netty.bootstrap.Bootstrap();
            io.netty.channel.nio.NioEventLoopGroup group = new io.netty.channel.nio.NioEventLoopGroup();
            
            bootstrap.group(group)
                    .channel(io.netty.channel.socket.nio.NioSocketChannel.class)
                    .handler(new io.netty.channel.ChannelInitializer<io.netty.channel.socket.SocketChannel>() {
                        @Override
                        protected void initChannel(io.netty.channel.socket.SocketChannel ch) {
                            io.netty.channel.ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new com.example.handler.RespEncoderHandler());
                            pipeline.addLast(new io.netty.handler.codec.ByteToMessageDecoder() {
                                @Override
                                protected void decode(io.netty.channel.ChannelHandlerContext ctx, io.netty.buffer.ByteBuf in, java.util.List<Object> out) {
                                    try {
                                        com.example.resp.Resp resp = com.example.resp.RespDecoder.decode(in);
                                        out.add(resp);
                                    } catch (Exception e) {
                                        // 半包，等待更多数据
                                        in.resetReaderIndex();
                                    }
                                }
                            });
                            pipeline.addLast(new io.netty.channel.SimpleChannelInboundHandler<Object>() {
                                @Override
                                protected void channelRead0(io.netty.channel.ChannelHandlerContext ctx, Object msg) {
                                    // 处理主服务器的响应
                                    if (msg instanceof com.example.resp.BulkString bulkString) {
                                        // 处理主服务器发送的 RDB 文件
                                        System.out.println("Received RDB file from master");
                                        
                                        try {
                                            // 清空自身数据
                                            System.out.println("Clearing slave data...");
                                            // 注意：这里需要根据实际的 Database 实现来清空数据
                                            // 假设 Database 类有一个 clear 方法来清空数据
                                            serverContext.db().clear();
                                            
                                            // 加载 RDB 文件
                                            System.out.println("Loading RDB file...");
                                            // 创建临时 RDB 文件
                                            java.nio.file.Path tempRdbFile = java.nio.file.Files.createTempFile("redis", ".rdb");
                                            // 写入 RDB 文件内容
                                            java.nio.file.Files.write(tempRdbFile, bulkString.getContent().getByteArray());
                                            // 加载 RDB 文件
                                            serverContext.getRdbModule().load(serverContext.db(), tempRdbFile);
                                            // 删除临时 RDB 文件
                                            java.nio.file.Files.delete(tempRdbFile);
                                            
                                            System.out.println("RDB file loaded successfully");
                                        } catch (Exception e) {
                                            System.err.println("Failed to load RDB file: " + e.getMessage());
                                        }
                                    } else if (msg instanceof com.example.resp.RespArray respArray) {
                                        // 处理主服务器发送的命令
                                        System.out.println("Received command from master: " + respArray);
                                        
                                        // 解析命令并执行
                                        try {
                                            com.example.command.Command command = com.example.command.CommandFactory.create(respArray);
                                            if (command != null) {
                                                command.execute(serverContext);
                                            }
                                        } catch (Exception e) {
                                            System.err.println("Failed to execute command from master: " + e.getMessage());
                                        }
                                    } else {
                                        // 处理其他响应
                                        System.out.println("Received response from master: " + msg);
                                    }
                                }
                            });
                        }
                    });
            
            // 连接到主服务器（异步）
            System.out.println("Connecting to master at " + serverContext.getMasterHost() + ":" + serverContext.getMasterPort());
            io.netty.channel.ChannelFuture future = bootstrap.connect(serverContext.getMasterHost(), serverContext.getMasterPort());
            
            // 监听连接结果
            future.addListener((io.netty.channel.ChannelFutureListener) f -> {
                if (f.isSuccess()) {
                    System.out.println("Connected to master at " + serverContext.getMasterHost() + ":" + serverContext.getMasterPort());
                    
                    // 更新复制状态
                    replicaState = ReplicaState.CONNECTED;
                    
                    // 发送SYNC命令
                    System.out.println("Sending SYNC command to master");
                    com.example.resp.RespArray syncCommand = new com.example.resp.RespArray(new com.example.resp.Resp[]{
                        new com.example.resp.BulkString("SYNC".getBytes())
                    });
                    f.channel().writeAndFlush(syncCommand);
                    
                    // 进入命令传播阶段
                    replicaState = ReplicaState.ONLINE;
                    System.out.println("Replication started successfully");
                } else {
                    System.err.println("Failed to connect to master: " + f.cause().getMessage());
                    replicaState = ReplicaState.DISCONNECTED;
                }
            });
            
        } catch (Exception e) {
            System.err.println("Failed to start replication: " + e.getMessage());
            replicaState = ReplicaState.DISCONNECTED;
        }
    }
    
    public void propagateCommand(Command command, ChannelHandlerContext ctx) {
        // 传播命令到所有从服务器
        if (serverContext.getRole() != ServerContext.ServerRole.MASTER) {
            return;
        }
        
        // 构建命令的RESP格式
        RespArray commandResp = new RespArray(((AbstractCommand) command).argv);
        
        // 遍历所有从服务器连接，发送命令
        for (ChannelHandlerContext slaveCtx : slaveConnections) {
            try {
                slaveCtx.writeAndFlush(commandResp);
            } catch (Exception e) {
                // 处理发送失败的情况
                System.err.println("Failed to propagate command to slave: " + e.getMessage());
            }
        }
    }
    
    public void addSlaveConnection(ChannelHandlerContext ctx) {
        // 添加从服务器连接
        slaveConnections.add(ctx);
        System.out.println("New slave connected, total slaves: " + slaveConnections.size());
    }
    
    public void removeSlaveConnection(ChannelHandlerContext ctx) {
        // 移除从服务器连接
        slaveConnections.remove(ctx);
        System.out.println("Slave disconnected, total slaves: " + slaveConnections.size());
    }
    
    public int getSlaveCount() {
        return slaveConnections.size();
    }
    
    public ReplicaState getReplicaState() {
        return replicaState;
    }
    
    public enum ReplicaState {
        DISCONNECTED,
        CONNECTING,
        CONNECTED,
        SYNCHRONIZING,
        ONLINE
    }
}
