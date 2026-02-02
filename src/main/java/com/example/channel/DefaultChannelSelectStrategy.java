package com.example.channel;


import com.example.channel.epoll.EpollChannelOption;
import com.example.channel.kqueue.KqueueChannelOption;
import com.example.channel.select.NioSelectChannelOption;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.kqueue.KQueue;
import io.netty.channel.socket.ServerSocketChannel;

public class DefaultChannelSelectStrategy implements ChannelSelectStrategy {
    @Override
    /**
     * 根据当前操作系统和运行环境，自动选择最合适的 Netty ServerSocketChannel 实现。
     */
    public LocalChannelOption<ServerSocketChannel> select() {

        if (KQueue.isAvailable()) {
            return new KqueueChannelOption();
        }
        if (Epoll.isAvailable()) {
            return new EpollChannelOption();
        }
        return new NioSelectChannelOption();
    }
}
