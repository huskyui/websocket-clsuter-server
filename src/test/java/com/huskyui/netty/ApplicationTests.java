package com.huskyui.netty;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.huskyui.netty.config.NettyProperties;
import com.huskyui.netty.server.message.SendMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
@Slf4j
class ApplicationTests {

    @Autowired
    NettyProperties nettyProperties;

    @Test
    void contextLoads() {
        log.info("{}", nettyProperties);
    }

    @Test
    public void testSendMsg() throws MQClientException, UnsupportedEncodingException, RemotingException, InterruptedException, MQBrokerException {
        // instantiate with a producer group name
        DefaultMQProducer producer = new DefaultMQProducer("default-producer-group");
        producer.setNamesrvAddr("192.168.149.88:9876");
        producer.start();
        //
        for (int i = 0; i < 1; i++) {
            // create message instance,specifying topic ,tag and message body
            Message msg = new Message("TopicTest","TagA",("Hello RocketMQ").getBytes(RemotingHelper.DEFAULT_CHARSET));
            SendResult sendResult = producer.send(msg);
            System.out.printf("%s%n",sendResult.toString());
        }
    }

    @Test
    public void testJackson() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        SendMessage sendMessage = mapper.readValue("{\"type\":\"login\",\"user\":\"huskyui\"}", SendMessage.class);
        System.out.println(sendMessage);

    }


    public static void main(String[] args) throws MQClientException {
        // Instantiate with specified consumer group name.
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("default-producer-group");

        // specify name server address
        consumer.setNamesrvAddr("192.168.149.88:9876");

        consumer.setMessageModel(MessageModel.CLUSTERING);

        // subscribe one more more topics to consume.
        consumer.subscribe("TopicTest","*");
        // register callback to execute on arrival of messages fetched from brokers.

        consumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
                System.out.printf("%s Receive New Messages: %s\n",Thread.currentThread().getName(),msgs);
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });

        // Launch the consumer instance.
        consumer.start();

        System.out.println("Consumer started.");
    }

    @Test
    public void testReceiveMsg() throws MQClientException {
        // Instantiate with specified consumer group name.
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("default-producer-group");

        // specify name server address
        consumer.setNamesrvAddr("192.168.149.88:9876");

        // subscribe one more more topics to consume.
        consumer.subscribe("TopicTest","*");
        // register callback to execute on arrival of messages fetched from brokers.

        consumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
                System.out.printf("%s Receive New Messages: %s\n",Thread.currentThread().getName(),msgs);
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });

        // Launch the consumer instance.
        consumer.start();

        System.out.println("Consumer started.");







    }


}
