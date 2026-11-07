package com.howcode.aichat.infrastructure.messaging;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** RocketMQ 配置。 */
@Data
@Component
@ConfigurationProperties(prefix = "aichat.rocketmq")
public class RocketMQProperties {
    private boolean enabled = true;
    private String nameServer = "127.0.0.1:9876";
    private String producerGroup = "aichat_producer";
    private String broadcastTopic = "aichat_broadcast";
    private String storeTopic = "aichat_store";
    private String broadcastConsumerGroup = "aichat_broadcast_consumer";
    private String storeConsumerGroup = "aichat_store_consumer";
    private int sendTimeoutMs = 3000;
}
