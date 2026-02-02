package com.example.channel.select;

import com.example.channel.LocalChannelOption;
import com.example.util.NamedThreadFactory;
import com.example.util.PropertiesUtil;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class NioSelectChannelOption implements LocalChannelOption<ServerSocketChannel> {

    private static final int DEFAULT_BOSS_THREADS = 1;

    private final NioEventLoopGroup boss;
    private final NioEventLoopGroup selectors;

    /**
     * 默认构造：从配置文件读取线程模型
     */
    public NioSelectChannelOption() {
        this(
                PropertiesUtil.getBossThreads(),
                PropertiesUtil.getIoThreads()
        );
    }

    /**
     * 显式线程数构造
     */
    public NioSelectChannelOption(int bossThreads, int ioThreads) {

        int finalBossThreads = normalizeBossThreads(bossThreads);
        int finalIoThreads = normalizeIoThreads(ioThreads);

        this.boss = new NioEventLoopGroup(
                finalBossThreads,
                NamedThreadFactory.boss()
        );

        this.selectors = new NioEventLoopGroup(
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
    public Class<NioServerSocketChannel> getChannelClass() {
        return NioServerSocketChannel.class;
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
