package com.example.handler;

import com.example.command.Command;
import com.example.command.CommandFactory;
import com.example.resp.Errors;
import com.example.resp.Resp;
import com.example.resp.RespArray;
import com.example.resp.RespDecoder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public final class CommandDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        in.markReaderIndex();

        try {
            // 1. RESP 协议解析
            Resp resp = RespDecoder.decode(in);

            // 2. Redis 命令必须是 Array
            if (!(resp instanceof RespArray respArray)) {
                // 协议层错误：不是数组
                throw new ProtocolException("Command must be RESP Array");
            }
            // 3. 交给工厂创建命令
            Command command = CommandFactory.create(respArray);
            if (command == null) {
                // 命令级错误：未知命令
                out.add(new Errors("ERR unknown command"));
                return;
            }

            out.add(command);
        } catch (IndexOutOfBoundsException e) {
            // 半包
            in.resetReaderIndex();

        } catch (ProtocolException | IllegalStateException e) {
            // RESP 协议错误
            ctx.close();

        } catch (Exception e) {
            // 命令构造阶段的异常 → 返回 ERR，不关连接
            out.add(new Errors("ERR " + e.getMessage()));
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // 防御性关闭
        ctx.close();
    }

    /**
     * 协议异常（RESP层）
     */
    static class ProtocolException extends RuntimeException {
        ProtocolException(String msg) {
            super(msg);
        }
    }
}
