package com.howcode.aichat.core.port.out;

import com.howcode.aichat.core.domain.model.ChatMessage;
import com.howcode.aichat.core.domain.model.RecallInfo;
import com.howcode.aichat.core.domain.model.RoomNotice;
import com.howcode.aichat.core.domain.model.UserInfo;

/**
 * 出站端口：向在线会话推送事件。
 * 由 transport 层的 Netty SessionRegistry 实现，core 不感知任何连接细节。
 */
public interface SessionGateway {
    void broadcastChat(ChatMessage message);

    void broadcastJoin(RoomNotice notice, UserInfo user);

    void broadcastLeave(RoomNotice notice, UserInfo user);

    void broadcastOffline(RoomNotice notice, UserInfo user);

    void broadcastRecall(RecallInfo info);

    void broadcastAi(ChatMessage message, int status);
}
