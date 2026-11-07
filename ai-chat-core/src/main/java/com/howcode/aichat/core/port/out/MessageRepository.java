package com.howcode.aichat.core.port.out;

import com.howcode.aichat.core.domain.model.ChatMessage;

import java.util.List;

/** 出站端口：聊天记录持久化（MySQL）。 */
public interface MessageRepository {
    void save(ChatMessage message);

    List<ChatMessage> findHistory(String roomId, int limit);
}
