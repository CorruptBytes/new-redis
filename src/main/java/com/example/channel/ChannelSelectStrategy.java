package com.example.channel;

import io.netty.channel.socket.ServerSocketChannel;

/**
 * @author Administrator
 */
public interface ChannelSelectStrategy {
    LocalChannelOption<ServerSocketChannel> select();
}
