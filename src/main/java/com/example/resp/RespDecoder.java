package com.example.resp;

import com.example.type.BytesWrapper;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public final class RespDecoder {

    public static Resp decode(ByteBuf in) {

        if (!in.isReadable()) {
            throw new IllegalStateException("empty buffer");
        }

        byte type = in.readByte();

        return switch (type) {
            case '+' -> new SimpleString(readLine(in));
            case '-' -> new Errors(readLine(in));
            case ':' -> new RespInt(readLong(in));
            case '$' -> readBulkString(in);
            case '*' -> readArray(in);
            default -> throw new IllegalArgumentException("Unknown RESP type: " + (char) type);
        };
    }

    private static BulkString readBulkString(ByteBuf in) {
        int len = (int) readLong(in);

        if (len == -1) {
            return BulkString.NULL;
        }

        if (in.readableBytes() < len + 2) {
            throw new IllegalStateException("incomplete bulk string");
        }

        byte[] data = new byte[len];
        in.readBytes(data);
        expectCRLF(in);

        return new BulkString(new BytesWrapper(data));
    }

    private static RespArray readArray(ByteBuf in) {
        int count = (int) readLong(in);
        Resp[] arr = new Resp[count];
        for (int i = 0; i < count; i++) {
            arr[i] = decode(in);
        }
        return new RespArray(arr);
    }

    private static long readLong(ByteBuf in) {
        long val = 0;
        boolean neg = false;

        byte b = in.readByte();
        if (b == '-') {
            neg = true;
            b = in.readByte();
        }

        while (b != '\r') {
            val = val * 10 + (b - '0');
            b = in.readByte();
        }

        if (in.readByte() != '\n') {
            throw new IllegalStateException("Invalid CRLF");
        }

        return neg ? -val : val;
    }

    private static String readLine(ByteBuf in) {
        StringBuilder sb = new StringBuilder();
        byte b;
        while ((b = in.readByte()) != '\r') {
            sb.append((char) b);
        }
        if (in.readByte() != '\n') {
            throw new IllegalStateException("Invalid CRLF");
        }
        return sb.toString();
    }

    private static void expectCRLF(ByteBuf in) {
        if (in.readByte() != '\r' || in.readByte() != '\n') {
            throw new IllegalStateException("Invalid CRLF");
        }
    }
}
