package com.example.channel.kqueue;

import com.example.channel.LocalChannelOption;
import com.example.util.NamedThreadFactory;
import com.example.util.PropertiesUtil;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.kqueue.KQueueEventLoopGroup;
import io.netty.channel.kqueue.KQueueServerSocketChannel;
import io.netty.channel.socket.ServerSocketChannel;

public class KqueueChannelOption implements LocalChannelOption<ServerSocketChannel> {

    private static final int DEFAULT_BOSS_THREADS = 1;

    private final KQueueEventLoopGroup boss;
    private final KQueueEventLoopGroup selectors;

    /**
     * 默认构造：从配置文件读取线程模型
     */
    public KqueueChannelOption() {
        this(
                PropertiesUtil.getBossThreads(),
                PropertiesUtil.getIoThreads()
        );
    }

    /**
     * 显式线程数构造
     */
    public KqueueChannelOption(int bossThreads, int ioThreads) {

        int finalBossThreads = normalizeBossThreads(bossThreads);
        int finalIoThreads = normalizeIoThreads(ioThreads);

        this.boss = new KQueueEventLoopGroup(
                finalBossThreads,
                NamedThreadFactory.boss()
        );

        this.selectors = new KQueueEventLoopGroup(
                finalIoThreads,
                NamedThreadFactory.selector()
        );
    }

    @Override
    public EventLoopGroup boss() {
        return boss;
    }

    @Override
    public EventLoopGroup selectors() {
        return selectors;
    }

    @Override
    public Class<KQueueServerSocketChannel> getChannelClass() {
        return KQueueServerSocketChannel.class;
    }

    /* ===================== normalize ===================== */

    private int normalizeBossThreads(int n) {
        return n > 0 ? n : DEFAULT_BOSS_THREADS;
    }

    private int normalizeIoThreads(int n) {
        if (n > 0) {
            return n;
        }
        return Runtime.getRuntime().availableProcessors() * 2;
    }
}
