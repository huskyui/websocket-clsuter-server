package com.huskyui.netty;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;

import java.util.List;

/**
 * @author 王鹏
 */
public class Consumer2 {
    public static void main(String[] args) throws MQClientException {
        // Instantiate with specified consumer group name.
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("default-producer-group");

        // specify name server address
        consumer.setNamesrvAddr("192.168.149.88:9876");

        // 将消息设置为广播模式,可以收到所有信息
        consumer.setMessageModel(MessageModel.BROADCASTING);

        // subscribe one more more topics to consume.
        consumer.subscribe("TopicTest", "*");
        // register callback to execute on arrival of messages fetched from brokers.

        consumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
                if (msgs != null && msgs.size() > 0) {
                    byte[] body = msgs.get(0).getBody();
                    System.out.printf("msgs size %d%s Receive New Messages: %s\n",msgs.size(),Thread.currentThread().getName(),new String(body));
                }
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });

        // Launch the consumer instance.
        consumer.start();

        System.out.println("Consumer1 started.");
    }
}
