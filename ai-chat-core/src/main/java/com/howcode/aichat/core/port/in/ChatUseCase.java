package com.howcode.aichat.core.port.in;

import com.howcode.aichat.core.domain.model.ChatMessage;

import java.util.List;

/** 入站端口：消息用例。 */
public interface ChatUseCase {
    void send(SendCommand command);

    List<ChatMessage> history(String roomId, String userId);

    void recall(String roomId, String messageId, String userId);

    /** 发送消息命令。 */
    record SendCommand(String messageId, String roomId, String senderId,
                       int messageType, String content, String ext) {
    }
}
