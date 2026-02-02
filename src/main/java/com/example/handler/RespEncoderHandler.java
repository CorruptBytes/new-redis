package com.example.handler;

import com.example.ServerContext;
import com.example.command.result.BlockedResult;
import com.example.database.BlockingListClient;
import com.example.database.Database;
import com.example.resp.Resp;
import com.example.resp.RespEncoder;
import com.example.type.RedisObject;
import com.example.type.RedisString;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import lombok.Setter;

import java.util.concurrent.TimeUnit;


public class RespEncoderHandler extends ChannelOutboundHandlerAdapter {
    @Override
    public void write(ChannelHandlerContext ctx,
                      Object msg,
                      ChannelPromise promise) {

        // 只处理 Resp 对象
        if (!(msg instanceof Resp resp)) {
            ctx.write(msg, promise); // 其它类型直接放行
            return;
        }

        ByteBuf out = ctx.alloc().buffer();
        try {
            RespEncoder.write(resp, out);
            ctx.write(out, promise); // 写出字节流
        } catch (Exception e) {
            out.release();
            promise.setFailure(e);
        }
    }
}

