package com.example.handler;

import com.example.ServerContext;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ConnectionHandler extends ChannelInboundHandlerAdapter {

    private final ServerContext serverContext;

    public ConnectionHandler(ServerContext serverContext) {
        this.serverContext = serverContext;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        // 连接建立时，只处理连接建立事件
        // 不自动将连接添加到从服务器连接列表中
        // 只有当连接发送 SYNC 命令时，才将其视为从服务器连接
        System.out.println("New connection established: " + ctx.channel().remoteAddress());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        // 连接断开时，从从服务器连接列表中移除
        serverContext.getReplicaManager().removeSlaveConnection(ctx);
        System.out.println("Connection closed: " + ctx.channel().remoteAddress());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // 处理异常，关闭连接
        cause.printStackTrace();
        ctx.close();
    }
}
