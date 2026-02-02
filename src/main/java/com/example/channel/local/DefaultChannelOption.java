package com.example.channel.local;

import com.example.channel.LocalChannelOption;
import com.example.util.NamedThreadFactory;
import com.example.util.PropertiesUtil;
import io.netty.channel.DefaultEventLoopGroup;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.local.LocalServerChannel;

public class DefaultChannelOption implements LocalChannelOption<LocalServerChannel> {

    private static final int DEFAULT_BOSS_THREADS = 1;

    private final DefaultEventLoopGroup boss;
    private final DefaultEventLoopGroup selectors;

    /**
     * 默认构造：从配置文件读取线程模型
     */
    public DefaultChannelOption() {
        this(
                PropertiesUtil.getBossThreads(),
                PropertiesUtil.getIoThreads()
        );
    }

    /**
     * 显式线程数构造
     */
    public DefaultChannelOption(int bossThreads, int ioThreads) {

        int finalBossThreads = normalizeBossThreads(bossThreads);
        int finalIoThreads = normalizeIoThreads(ioThreads);

        this.boss = new DefaultEventLoopGroup(
                finalBossThreads,
                NamedThreadFactory.boss()
        );

        this.selectors = new DefaultEventLoopGroup(
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
    public Class<LocalServerChannel> getChannelClass() {
        return LocalServerChannel.class;
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
