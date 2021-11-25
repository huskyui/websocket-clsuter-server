package com.huskyui.netty;

import com.huskyui.netty.config.NettyProperties;
import com.huskyui.netty.server.WebSocketServer;
import com.huskyui.netty.server.handler.WebSocketChannelInitializer;
import com.huskyui.netty.store.ChannelRepo;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableConfigurationProperties({NettyProperties.class})
public class Application {
    @Autowired
    private NettyProperties nettyProperties;
    @Autowired
    private WebSocketChannelInitializer webSocketChannelInitializer;


    public static void main(String[] args) throws Exception {
        ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);
        WebSocketServer bean = context.getBean(WebSocketServer.class);
        bean.start();
    }


    @Bean(destroyMethod = "shutdownGracefully")
    public NioEventLoopGroup bossGroup(){
        return new NioEventLoopGroup(nettyProperties.getBossCount());
    }

    @Bean(destroyMethod = "shutdownGracefully")
    public NioEventLoopGroup workerGroup(){
        return new NioEventLoopGroup(nettyProperties.getWorkerCount());
    }

    @Bean
    public ChannelRepo channelRepo(){
        return new ChannelRepo();
    }

    @Bean
    public ServerBootstrap bootstrap(){
        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup(),workerGroup())
                .channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.TRACE))
                .childHandler(webSocketChannelInitializer);
        return b;
    }

}
