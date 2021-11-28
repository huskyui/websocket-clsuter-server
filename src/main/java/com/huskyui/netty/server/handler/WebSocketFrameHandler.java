package com.huskyui.netty.server.handler;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.huskyui.netty.server.message.SendMessage;
import com.huskyui.netty.store.ChannelRepo;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.common.RemotingHelper;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 王鹏
 */
@ChannelHandler.Sharable
@Slf4j
public class WebSocketFrameHandler extends SimpleChannelInboundHandler<WebSocketFrame> {
    private ChannelRepo channelRepo;

    private DefaultMQProducer producer;


    public WebSocketFrameHandler(ChannelRepo channelRepo, DefaultMQProducer producer) {
        this.channelRepo = channelRepo;
        this.producer = producer;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, WebSocketFrame frame) throws Exception {
        if (frame instanceof TextWebSocketFrame) {

            Map<String, String> response = new HashMap<>();

            String requestJson = ((TextWebSocketFrame) frame).text();
            System.out.println(requestJson);
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            try {
                SendMessage sendMessage = mapper.readValue(requestJson, SendMessage.class);
                if ("login".equals(sendMessage.getType())) {
                    Channel channel = channelHandlerContext.channel();
                    channelRepo.put(sendMessage.getUser(), channel);
                    response.clear();
                    response.put("msg", "登录成功");
                    channel.writeAndFlush(new TextWebSocketFrame(Unpooled.copiedBuffer(mapper.writeValueAsString(response), CharsetUtil.UTF_8)));
                } else if ("send".equals(sendMessage.getType())) {
                    Channel channel = channelRepo.get(sendMessage.getToUser());
                    response.put("来自用户", sendMessage.getUser());
                    response.put("发送消息", sendMessage.getMessage());
                    if (channel != null) {
                        channel.writeAndFlush(new TextWebSocketFrame(Unpooled.copiedBuffer(mapper.writeValueAsString(response), CharsetUtil.UTF_8)));
                    } else {
                        // 通过mq广播
                        Message message = new Message("TopicTest", "TagA", (mapper.writeValueAsString(sendMessage)).getBytes(RemotingHelper.DEFAULT_CHARSET));
                        SendResult send = producer.send(message);
                        channelHandlerContext.writeAndFlush(new TextWebSocketFrame("发送到队列中"+mapper.writeValueAsString(sendMessage)));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            String message = "unsupported frame type: " + frame.getClass().getName();
            throw new UnsupportedOperationException(message);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
//        super.channelActive(ctx);
        ctx.fireChannelActive();
        channelRepo.put(ctx.channel());
        ctx.writeAndFlush(new TextWebSocketFrame(Unpooled.copiedBuffer("恭喜你绑定成功", Charset.defaultCharset())));
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
//        super.channelInactive(ctx);
        ctx.fireChannelInactive();
        // TODO 需要保存一份关于 channel->用户唯一标识的
        // 这样可以保证在发送不成功时，可以成功
        channelRepo.remove(ctx.channel());
    }
}
