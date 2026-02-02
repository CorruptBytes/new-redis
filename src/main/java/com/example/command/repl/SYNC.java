package com.example.command.repl;

import com.example.ServerContext;
import com.example.command.AbstractCommand;
import com.example.command.CommandType;
import com.example.command.result.CommandResult;
import com.example.command.result.SimpleStringResult;
import io.netty.channel.ChannelHandlerContext;

public class SYNC extends AbstractCommand {
    private ChannelHandlerContext ctx;

    public void setChannelHandlerContext(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public CommandType type() {
        return CommandType.SYNC;
    }
    
    @Override
    public CommandResult execute(ServerContext serverContext) {
        // 处理从服务器的 SYNC 命令
        // 1. 生成并发送 RDB 文件
        // 2. 进入命令传播阶段
        System.out.println("Received SYNC command from slave");
        
        // 生成 RDB 文件并发送给从节点
        if (ctx != null) {
            try {
                // 创建临时 RDB 文件
                java.nio.file.Path tempRdbFile = java.nio.file.Files.createTempFile("redis", ".rdb");
                
                // 生成 RDB 文件
                System.out.println("Generating RDB file for slave...");
                serverContext.getRdbModule().save(serverContext.db(), tempRdbFile);
                
                // 读取 RDB 文件内容
                byte[] rdbContent = java.nio.file.Files.readAllBytes(tempRdbFile);
                
                // 发送 RDB 文件内容给从节点
                System.out.println("Sending RDB file to slave...");
                ctx.writeAndFlush(new com.example.resp.BulkString(rdbContent));
                
                // 删除临时 RDB 文件
                java.nio.file.Files.delete(tempRdbFile);
                
                // 将连接添加到从服务器连接列表中
                serverContext.getReplicaManager().addSlaveConnection(ctx);
                
                System.out.println("RDB file sent successfully to slave");
            } catch (Exception e) {
                System.err.println("Failed to send RDB file to slave: " + e.getMessage());
            }
        }
        
        // 简化实现：返回 OK，表示进入命令传播阶段
        return new SimpleStringResult("OK");
    }
}
