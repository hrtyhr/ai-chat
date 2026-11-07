package com.howcode.aichat.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;

/** RocketMQ 消息体的 JSON 编解码。 */
public final class MqCodec {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private MqCodec() {
    }

    public static byte[] encode(Object value) {
        try {
            return MAPPER.writeValueAsBytes(value);
        } catch (Exception e) {
            throw new IllegalStateException("MQ 消息序列化失败", e);
        }
    }

    public static <T> T decode(byte[] body, Class<T> type) {
        try {
            return MAPPER.readValue(body, type);
        } catch (Exception e) {
            throw new IllegalStateException("MQ 消息反序列化失败", e);
        }
    }
}
