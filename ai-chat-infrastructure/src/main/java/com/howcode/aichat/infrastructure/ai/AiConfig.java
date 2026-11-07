package com.howcode.aichat.infrastructure.ai;

import com.howcode.aichat.core.port.out.AiGateway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** 按是否配置 apiKey 选择真实网关或本地 Mock。 */
@Configuration
public class AiConfig {

    @Bean
    public AiGateway aiGateway(AiProperties properties) {
        if (null == properties.getApiKey() || properties.getApiKey().isBlank()) {
            return new MockAiGateway();
        }
        return new OpenAiAiGateway(properties);
    }
}
