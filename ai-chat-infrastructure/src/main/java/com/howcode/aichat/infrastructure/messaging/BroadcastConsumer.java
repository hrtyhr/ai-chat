package com.howcode.aichat.infrastructure.messaging;

import com.howcode.aichat.core.domain.model.ChatMessage;
import com.howcode.aichat.core.port.out.SessionGateway;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/** 广播消费者：每个节点都消费，收到后向本机该房间的会话群发。 */
@Component
@ConditionalOnProperty(prefix = "aichat.rocketmq", name = "enabled", havingValue = "true")
public class BroadcastConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(BroadcastConsumer.class);

    private final RocketMQProperties properties;
    private final SessionGateway sessionGateway;
    private DefaultMQPushConsumer consumer;

    public BroadcastConsumer(RocketMQProperties properties, SessionGateway sessionGateway) {
        this.properties = properties;
        this.sessionGateway = sessionGateway;
    }

    @PostConstruct
    public void start() throws Exception {
        consumer = new DefaultMQPushConsumer(properties.getBroadcastConsumerGroup());
        consumer.setNamesrvAddr(properties.getNameServer());
        consumer.setMessageModel(MessageModel.BROADCASTING);
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
        consumer.subscribe(properties.getBroadcastTopic(), "*");
        consumer.registerMessageListener((MessageListenerConcurrently) (messages, context) -> {
            for (MessageExt message : messages) {
                try {
                    sessionGateway.broadcastChat(MqCodec.decode(message.getBody(), ChatMessage.class));
                } catch (Exception e) {
                    LOGGER.error("广播消息处理失败, rocketMsgId = {}", message.getMsgId(), e);
                }
            }
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        });
        consumer.start();
        LOGGER.info("RocketMQ 广播消费者启动成功, topic = {}", properties.getBroadcastTopic());
    }

    @PreDestroy
    public void stop() {
        if (null != consumer) {
            consumer.shutdown();
        }
    }
}
