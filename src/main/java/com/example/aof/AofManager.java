package com.example.aof;

import com.example.ServerContext;
import com.example.database.Database;
import com.example.resp.RespArray;
import com.example.resp.RespEncoder;
import com.example.type.RedisObject;
import com.example.type.RedisString;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.EventLoop;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.util.List;

public final class AofManager {

    private final File file;
    private FileChannel channel;
    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream(1024 * 16);
    private final Database<RedisString,RedisObject> database;
    private final AofRewriteContext rewriteContext;
    private boolean isRewrite = false;
    private List<EntryView<RedisString,RedisObject>> snapshot = null;
    public AofManager(String path,Database<RedisString,RedisObject> database) {
        try {
            this.database = database;
            this.file = new File(path);
            // 创建目录（如果不存在）
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
                System.out.println("Created AOF directory: " + parentDir.getAbsolutePath());
            }
            this.channel = FileChannel.open(
                    file.toPath(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.WRITE,
                    StandardOpenOption.APPEND
            );
            this.rewriteContext = new AofRewriteContext(database);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public synchronized void startRewrite(EventLoop mainLoop) {
        if (isRewrite) return;

        isRewrite = true;

        // 1. 标记 COW（主线程，极短）
        database.markAllObjectsCow();

        // 2. 拿 snapshot（只是引用，不复制数据）
        snapshot = database.entriesSnapshot();

        // 3. 启动后台 rewrite
        rewriteContext.start(snapshot, this,mainLoop);
    }

    public synchronized void completedRewrite() {
        try {
            rewriteContext.finish();

            channel.close();
            rewriteContext.replaceAofFile(file);

            channel = FileChannel.open(
                    file.toPath(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.WRITE,
                    StandardOpenOption.APPEND
            );
            for (EntryView<RedisString, RedisObject> entryView : snapshot) {
                entryView.value().clearCow();
            }
            isRewrite = false;
        } catch (Exception e) {
            for (EntryView<RedisString, RedisObject> entryView : snapshot) {
                entryView.value().clearCow();
            }  // 失败也要清
            isRewrite = false;
        }
    }

    public void append(RespArray command) {
        try {
            ByteBuf buf = Unpooled.buffer();
            RespEncoder.write(command, buf);
            byte[] data = new byte[buf.readableBytes()];
            buf.readBytes(data);
            buf.release();

            buffer.write(data);
            // rewrite 期间，写入 rewrite buffer
            if (isRewrite) {
                rewriteContext.appendToBuf(data);
            }
        } catch (Exception e) {
            // Redis 风格：AOF 错误不直接崩溃
        }
    }

    public void flush() {
        try {
            if (buffer.size() == 0) return;
            ByteBuffer nioBuf = ByteBuffer.wrap(buffer.toByteArray());
            buffer.reset();
            channel.write(nioBuf);
            channel.force(false); // 对应 appendfsync always
        } catch (IOException e) {
            // ignore
        }
    }

    public void load(Database<RedisString, RedisObject> database) {
        new AofLoader(file,new ServerContext(database,this,null)).load(database);
    }

    public void close() {
        try {
            flush();
            channel.close();
        } catch (IOException ignored) {}
    }
}



