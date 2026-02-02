package com.example.util;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public final class NamedThreadFactory implements ThreadFactory {

    private final String prefix;
    private final AtomicInteger index = new AtomicInteger(0);

    public NamedThreadFactory(String prefix) {
        this.prefix = prefix;
    }

    public static ThreadFactory boss() {
        return new NamedThreadFactory("Server_boss_");
    }

    public static ThreadFactory selector() {
        return new NamedThreadFactory("Server_selector_");
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(r, prefix + index.getAndIncrement());
        t.setDaemon(false);
        return t;
    }
}
