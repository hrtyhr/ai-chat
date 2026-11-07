package com.howcode.aichat.infrastructure.messaging;

import com.howcode.aichat.core.domain.model.ChatMessage;
import com.howcode.aichat.core.port.out.MessagePublisher;
import com.howcode.aichat.core.port.out.MessageRepository;
import com.howcode.aichat.core.port.out.SessionGateway;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * 消息发布适配器：优先走 RocketMQ；MQ 关闭或发送失败时自动降级为本地广播/落库。
 */
@Component
public class RocketMQMessagePublisher implements MessagePublisher {

    private static final Logger LOGGER = LoggerFactory.getLogger(RocketMQMessagePublisher.class);

    private final RocketMQProperties properties;
    private final ObjectProvider<DefaultMQProducer> producerProvider;
    private final SessionGateway sessionGateway;
    private final MessageRepository messageRepository;

    public RocketMQMessagePublisher(RocketMQProperties properties,
                                    ObjectProvider<DefaultMQProducer> producerProvider,
                                    SessionGateway sessionGateway,
                                    MessageRepository messageRepository) {
        this.properties = properties;
        this.producerProvider = producerProvider;
        this.sessionGateway = sessionGateway;
        this.messageRepository = messageRepository;
    }

    @Override
    public void publishBroadcast(ChatMessage message) {
        if (properties.isEnabled() && trySend(properties.getBroadcastTopic(), message)) {
            return;
        }
        sessionGateway.broadcastChat(message);
    }

    @Override
    public void publishStore(ChatMessage message) {
        if (properties.isEnabled() && trySend(properties.getStoreTopic(), message)) {
            return;
        }
        try {
            messageRepository.save(message);
        } catch (Exception e) {
            LOGGER.error("本地消息落库失败, messageId = {}", message.getMessageId(), e);
        }
    }

    private boolean trySend(String topic, ChatMessage message) {
        DefaultMQProducer producer = producerProvider.getIfAvailable();
        if (null == producer) {
            return false;
        }
        try {
            producer.send(new Message(topic, MqCodec.encode(message)), properties.getSendTimeoutMs());
            return true;
        } catch (Exception e) {
            LOGGER.error("RocketMQ 发送失败, topic = {}, messageId = {}", topic, message.getMessageId(), e);
            return false;
        }
    }
}
