package com.huskyui.netty.server;

import com.huskyui.netty.config.NettyProperties;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;

/**
 * @author 王鹏
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketServer {
    private final ServerBootstrap serverBootstrap;
    private final NettyProperties nettyProperties;

    private Channel serverChannel;

    public void start() throws Exception {
        System.out.println("netty server start at port" + nettyProperties.getWebsocketPort());
        serverChannel = serverBootstrap
                .bind(nettyProperties.getWebsocketPort()).sync().channel().closeFuture().sync().channel();
    }

    @PreDestroy
    public void stop() {
        if (serverChannel != null) {
            serverChannel.close();
            serverChannel.parent().close();
        }
    }
}
