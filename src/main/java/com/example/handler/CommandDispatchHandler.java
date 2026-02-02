package com.example.handler;

import com.example.command.Command;
import com.example.command.repl.SYNC;
import com.example.evnetloop.MainEventLoop;
import com.example.resp.Errors;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public final class CommandDispatchHandler extends ChannelInboundHandlerAdapter {

    private final MainEventLoop eventLoop;

    public CommandDispatchHandler(MainEventLoop eventLoop) {
        this.eventLoop = eventLoop;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof Errors err) {
            ctx.writeAndFlush(err);
            return;
        }

        if (!(msg instanceof Command command)) {
            ctx.writeAndFlush(new Errors("ERR invalid command"));
            return;
        }

        // 如果是 SYNC 命令，设置 ChannelHandlerContext
        if (command instanceof SYNC syncCommand) {
            syncCommand.setChannelHandlerContext(ctx);
        }

        // 提交给 Redis 主事件循环处理
        eventLoop.submit(command, ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}



