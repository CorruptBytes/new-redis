package com.example.aof;

import com.example.ServerContext;
import com.example.command.Command;
import com.example.command.CommandFactory;
import com.example.database.Database;
import com.example.resp.Resp;
import com.example.resp.RespArray;
import com.example.resp.RespDecoder;
import com.example.type.RedisObject;
import com.example.type.RedisString;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.File;
import java.io.FileInputStream;

public final class AofLoader {

    private final File file;
    private final ServerContext context;
    public AofLoader(File file, ServerContext context) {
        this.file = file;
        this.context = context;
    }

    public void load(Database<RedisString, RedisObject> database) {
        if (!file.exists() || file.length() == 0) return;

        try (FileInputStream in = new FileInputStream(file)) {
            ByteBuf buf = Unpooled.buffer();
            buf.writeBytes(in.readAllBytes());

            while (buf.isReadable()) {
                Resp resp = RespDecoder.decode(buf);
                if (!(resp instanceof RespArray array)) {
                    throw new RuntimeException("Invalid AOF format");
                }

                Command cmd = CommandFactory.create(array);
                cmd.setContent(array.getArray());
                cmd.execute(context);
            }
        } catch (Exception e) {
            throw new RuntimeException("AOF load failed", e);
        }
    }
}
