package com.example.channel.epoll;

import com.example.channel.LocalChannelOption;
import com.example.util.NamedThreadFactory;
import com.example.util.PropertiesUtil;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.socket.ServerSocketChannel;

public class EpollChannelOption implements LocalChannelOption<ServerSocketChannel> {

    private static final int DEFAULT_BOSS_THREADS = 1;

    private final EpollEventLoopGroup boss;
    private final EpollEventLoopGroup selectors;

    /**
     * 默认构造：直接从配置文件读取
     */
    public EpollChannelOption() {
        this(
                PropertiesUtil.getBossThreads(),
                PropertiesUtil.getIoThreads()
        );
    }

    /**
     * 显式线程数构造（更通用）
     */
    public EpollChannelOption(int bossThreads, int ioThreads) {

        int finalBossThreads = normalizeBossThreads(bossThreads);
        int finalIoThreads = normalizeIoThreads(ioThreads);

        this.boss = new EpollEventLoopGroup(
                finalBossThreads,
                NamedThreadFactory.boss()
        );

        this.selectors = new EpollEventLoopGroup(
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
    public Class<EpollServerSocketChannel> getChannelClass() {
        return EpollServerSocketChannel.class;
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

