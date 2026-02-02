package com.example.aof;

import com.example.database.Database;
import com.example.resp.RespArray;
import com.example.resp.RespEncoder;
import com.example.type.RedisObject;
import com.example.type.RedisString;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.EventLoop;
import lombok.Getter;
import lombok.Setter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.List;
@Setter
public class AofRewriteContext {

    private final Database<RedisString, RedisObject> database;

    private final ByteArrayOutputStream rewriteBuffer =
            new ByteArrayOutputStream(1024 * 16);

    private File tempFile;
    private FileChannel tempChannel;

    public AofRewriteContext(Database<RedisString, RedisObject> database) {
        this.database = database;
    }

    public void start(List<EntryView<RedisString, RedisObject>> snapshot,
                      AofManager manager, EventLoop mainLoop) {

        new Thread(() -> {
            try {
                rewrite(snapshot);

                // 🔥 回调投递到主线程
                mainLoop.execute(manager::completedRewrite);

            } catch (Exception ignored) {
                mainLoop.execute(manager::completedRewrite);
            }
        }, "aof-rewrite").start();
    }

    public void rewrite(List<EntryView<RedisString, RedisObject>> snapshot)
            throws IOException {

        tempFile = new File("appendonly.aof.rewrite");
        tempChannel = FileChannel.open(
                tempFile.toPath(),
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE,
                StandardOpenOption.TRUNCATE_EXISTING
        );

        for (EntryView<RedisString, RedisObject> e : snapshot) {
            writeEntry(e);
        }
    }
    private void writeEntry(EntryView<RedisString, RedisObject> e)
            throws IOException {

        RedisString key = e.key();
        RedisObject val = e.value();

        // 1. 根据对象类型生成写命令
        List<RespArray> cmds = AofCommandBuilder.build(e);
        for (RespArray cmd : cmds) {
            writeResp(cmd);
        }
    }
    private void writeResp(RespArray resp) throws IOException {
        ByteBuf buf = Unpooled.buffer();
        RespEncoder.write(resp, buf);

        ByteBuffer nio = ByteBuffer.allocate(buf.readableBytes());
        buf.readBytes(nio);
        buf.release();

        nio.flip();
        tempChannel.write(nio);
    }
    public synchronized void appendToBuf(byte[] data) {
        try {
            rewriteBuffer.write(data);
        } catch (IOException ignored) {}
    }
    public void finish() throws IOException {
        tempChannel.write(
                ByteBuffer.wrap(rewriteBuffer.toByteArray())
        );
        tempChannel.force(false);
        tempChannel.close();
    }
    public void replaceAofFile(File target) throws IOException {
        Files.move(
                tempFile.toPath(),
                target.toPath(),
                StandardCopyOption.REPLACE_EXISTING,
                StandardCopyOption.ATOMIC_MOVE
        );
    }


}


