package com.huskyui.netty.server.handler;

import com.huskyui.netty.config.NettyProperties;
import com.huskyui.netty.store.ChannelRepo;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author 王鹏
 */
@Component
public class WebSocketChannelInitializer extends ChannelInitializer<SocketChannel> {
    @Resource
    private NettyProperties nettyProperties;
    @Resource
    private ChannelRepo channelRepo;
    @Resource
    private DefaultMQProducer producer;

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new HttpServerCodec())
                .addLast(new HttpObjectAggregator(65536))
                .addLast(new WebSocketServerCompressionHandler())
                .addLast(new WebSocketServerProtocolHandler(nettyProperties.getWebsocketPath(), null, true))
                .addLast(new WebSocketFrameHandler(channelRepo, producer));
    }
}
