package com.howcode.aichat.infrastructure.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.howcode.aichat.core.port.out.AiGateway;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/** OpenAI 兼容网关（适配 OpenAI / DeepSeek / 通义千问兼容模式）。 */
public class OpenAiAiGateway implements AiGateway {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final AiProperties properties;
    private final HttpClient httpClient;
    private final String endpoint;

    public OpenAiAiGateway(AiProperties properties) {
        this.properties = properties;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(properties.getTimeoutSeconds()))
                .build();
        String base = properties.getBaseUrl();
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        this.endpoint = base + "/chat/completions";
    }

    @Override
    public String chat(String userId, String userName, String prompt) {
        try {
            ObjectNode body = MAPPER.createObjectNode();
            body.put("model", properties.getModel());
            ArrayNode messages = body.putArray("messages");
            messages.addObject().put("role", "system").put("content", properties.getSystemPrompt());
            messages.addObject().put("role", "user").put("content", prompt);

            HttpRequest request = HttpRequest.newBuilder(URI.create(endpoint))
                    .timeout(Duration.ofSeconds(properties.getTimeoutSeconds()))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + properties.getApiKey())
                    .POST(HttpRequest.BodyPublishers.ofString(MAPPER.writeValueAsString(body)))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() / 100 != 2) {
                throw new IllegalStateException("AI 接口返回状态码 " + response.statusCode());
            }
            JsonNode content = MAPPER.readTree(response.body())
                    .path("choices").path(0).path("message").path("content");
            return content.isMissingNode() || content.isNull() ? "" : content.asText();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("AI 调用被中断", e);
        } catch (Exception e) {
            throw new IllegalStateException("AI 调用失败: " + e.getMessage(), e);
        }
    }
}
