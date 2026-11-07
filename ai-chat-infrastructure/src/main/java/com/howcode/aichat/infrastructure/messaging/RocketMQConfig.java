package com.howcode.aichat.infrastructure.messaging;

import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** RocketMQ Producer 装配（开关控制）。 */
@Configuration
@ConditionalOnProperty(prefix = "aichat.rocketmq", name = "enabled", havingValue = "true")
public class RocketMQConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(RocketMQConfig.class);

    @Bean(destroyMethod = "shutdown")
    public DefaultMQProducer defaultMQProducer(RocketMQProperties properties) throws MQClientException {
        DefaultMQProducer producer = new DefaultMQProducer(properties.getProducerGroup());
        producer.setNamesrvAddr(properties.getNameServer());
        producer.setSendMsgTimeout(properties.getSendTimeoutMs());
        producer.start();
        LOGGER.info("RocketMQ Producer 启动成功, namesrv = {}", properties.getNameServer());
        return producer;
    }
}
