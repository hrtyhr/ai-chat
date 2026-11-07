package com.howcode.aichat.core.port.out;

import com.howcode.aichat.core.domain.model.ChatMessage;

/** 出站端口：消息发布（异步广播 / 异步落库）。 */
public interface MessagePublisher {
    void publishBroadcast(ChatMessage message);

    void publishStore(ChatMessage message);
}
