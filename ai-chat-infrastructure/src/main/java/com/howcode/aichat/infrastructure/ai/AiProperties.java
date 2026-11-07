package com.howcode.aichat.infrastructure.ai;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** AI 配置。 */
@Data
@Component
@ConfigurationProperties(prefix = "aichat.ai")
public class AiProperties {
    private String apiKey;
    private String baseUrl = "https://api.openai.com/v1";
    private String model = "gpt-4o-mini";
    private String systemPrompt = "你是一个友好、简洁的聊天助手。";
    private int timeoutSeconds = 30;
}
