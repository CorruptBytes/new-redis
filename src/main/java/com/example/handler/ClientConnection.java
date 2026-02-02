package com.example.handler;

import com.example.command.result.CommandResult;
import com.example.resp.RespEncoder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public class ClientConnection {
    private final ChannelHandlerContext ctx;

    public ClientConnection(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

    public void reply(CommandResult result) {
        if (result == null) return;
        ByteBuf out = ctx.alloc().buffer();
        try {
            RespEncoder.write(result.toResp(), out);
            ctx.writeAndFlush(out);
        } catch (Exception e) {
            out.release();
            e.printStackTrace();
        }
    }
}