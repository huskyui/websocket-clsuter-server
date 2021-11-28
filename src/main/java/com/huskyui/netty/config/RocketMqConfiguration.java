package com.huskyui.netty.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.huskyui.netty.server.message.SendMessage;
import com.huskyui.netty.store.ChannelRepo;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.CharsetUtil;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 王鹏
 */
@Configuration
public class RocketMqConfiguration {
    @Value("${rocketmq.namesrv.address}")
    private String rockerMqNameSrv;

    @Resource
    private ChannelRepo channelRepo;

    @Bean
    public DefaultMQProducer producer() {
        DefaultMQProducer producer = new DefaultMQProducer("default-producer-group");
        producer.setNamesrvAddr(rockerMqNameSrv);
        try {
            producer.start();
        } catch (MQClientException e) {
            throw new RuntimeException("rocket mq start error!");
        }
        return producer;
    }

    @Bean
    public DefaultMQPushConsumer consumer() throws MQClientException {
        // Instantiate with specified consumer group name.
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("default-producer-group");

        // specify name server address
        consumer.setNamesrvAddr("192.168.149.88:9876");

        consumer.setMessageModel(MessageModel.BROADCASTING);

        // subscribe one more more topics to consume.
        try {
            consumer.subscribe("TopicTest", "*");
        } catch (Exception e) {
            throw new RuntimeException("");
        }
        // register callback to execute on arrival of messages fetched from brokers.

        consumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
                MessageExt messageExt = msgs.get(0);
                String messageJson = new String(messageExt.getBody(),CharsetUtil.UTF_8);
                ObjectMapper mapper = new ObjectMapper();
                try {
                    SendMessage sendMessage = mapper.readValue(messageJson, SendMessage.class);
                    String toUser = sendMessage.getToUser();
                    Channel channel = channelRepo.get(toUser);
                    if (channel!=null){
                        Map<String,String> response = new HashMap<>();
                        response.put("来自队列用户",sendMessage.getUser());
                        response.put("消息",sendMessage.getMessage());
                        channel.writeAndFlush(new TextWebSocketFrame(Unpooled.copiedBuffer(mapper.writeValueAsString(response), CharsetUtil.UTF_8)));
                    }
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }


                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });

        // Launch the consumer instance.
        consumer.start();
        return consumer;
    }


}
