package com.howcode.aichat.infrastructure.messaging;

import com.howcode.aichat.core.domain.model.ChatMessage;
import com.howcode.aichat.core.port.out.MessageRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

/** 落库消费者：集群消费，同一消费组内只有一个节点写 MySQL。 */
@Component
@ConditionalOnProperty(prefix = "aichat.rocketmq", name = "enabled", havingValue = "true")
public class StoreConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(StoreConsumer.class);

    private final RocketMQProperties properties;
    private final MessageRepository messageRepository;
    private DefaultMQPushConsumer consumer;

    public StoreConsumer(RocketMQProperties properties, MessageRepository messageRepository) {
        this.properties = properties;
        this.messageRepository = messageRepository;
    }

    @PostConstruct
    public void start() throws Exception {
        consumer = new DefaultMQPushConsumer(properties.getStoreConsumerGroup());
        consumer.setNamesrvAddr(properties.getNameServer());
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
        consumer.subscribe(properties.getStoreTopic(), "*");
        consumer.registerMessageListener((MessageListenerConcurrently) (messages, context) -> {
            for (MessageExt message : messages) {
                try {
                    messageRepository.save(MqCodec.decode(message.getBody(), ChatMessage.class));
                } catch (DuplicateKeyException e) {
                    LOGGER.info("消息已存在，跳过重复落库, rocketMsgId = {}", message.getMsgId());
                } catch (Exception e) {
                    LOGGER.error("消息落库失败, rocketMsgId = {}", message.getMsgId(), e);
                    return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                }
            }
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        });
        consumer.start();
        LOGGER.info("RocketMQ 落库消费者启动成功, topic = {}", properties.getStoreTopic());
    }

    @PreDestroy
    public void stop() {
        if (null != consumer) {
            consumer.shutdown();
        }
    }
}
