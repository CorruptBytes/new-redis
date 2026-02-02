package com.example.resp;

import io.netty.buffer.ByteBuf;

public final class RespEncoder {

    private RespEncoder() {}

    public static void write(Resp resp, ByteBuf out) {

        if (resp instanceof SimpleString s) {
            out.writeByte('+');
            writeAscii(out, s.getContent());
            writeCRLF(out);
            return;
        }

        if (resp instanceof Errors e) {
            out.writeByte('-');
            writeAscii(out, e.getContent());
            writeCRLF(out);
            return;
        }

        if (resp instanceof RespInt i) {
            out.writeByte(':');
            writeAscii(out, Long.toString(i.getValue()));
            writeCRLF(out);
            return;
        }

        if (resp instanceof BulkString b) {
            out.writeByte('$');

            if (b.isNull()) {
                writeAscii(out, "-1");
                writeCRLF(out);
                return;
            }

            byte[] bytes = b.getContent().getByteArray();
            writeAscii(out, Integer.toString(bytes.length));
            writeCRLF(out);
            out.writeBytes(bytes);
            writeCRLF(out);
            return;
        }

        if (resp instanceof RespArray a) {
            out.writeByte('*');
            writeAscii(out, Integer.toString(a.getArray().length));
            writeCRLF(out);
            for (Resp r : a.getArray()) {
                write(r, out);
            }
            return;
        }

        throw new IllegalArgumentException("Unknown RESP type");
    }

    private static void writeAscii(ByteBuf out, String s) {
        for (int i = 0; i < s.length(); i++) {
            out.writeByte((byte) s.charAt(i));
        }
    }

    private static void writeCRLF(ByteBuf out) {
        out.writeByte('\r');
        out.writeByte('\n');
    }
}
