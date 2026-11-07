package com.howcode.aichat.transport.session;

import com.google.protobuf.GeneratedMessageV3;
import com.howcode.aichat.core.domain.model.ChatMessage;
import com.howcode.aichat.core.domain.model.RecallInfo;
import com.howcode.aichat.core.domain.model.RoomNotice;
import com.howcode.aichat.core.domain.model.UserInfo;
import com.howcode.aichat.core.port.out.SessionGateway;
import com.howcode.aichat.transport.dispatch.ResponseFactory;
import com.howcode.aichat.transport.protocol.ProtocolCodec;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 会话注册表（接入层）：维护连接、用户、房间三种索引，并实现出站端口 SessionGateway。
 */
@Component
public class SessionRegistry implements SessionGateway {

    private final Map<String, ChatSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, ChatSession> userSessions = new ConcurrentHashMap<>();
    private final Map<String, Set<ChatSession>> roomSessions = new ConcurrentHashMap<>();

    @Resource
    private ProtocolCodec codec;

    public void register(ChatSession session) {
        sessions.put(session.id(), session);
    }

    public ChatSession byChannelId(String channelId) {
        return sessions.get(channelId);
    }

    public void bind(String userId, ChatSession session) {
        userSessions.put(userId, session);
    }

    public void unbind(String userId) {
        userSessions.remove(userId);
    }

    public void joinRoom(String roomId, ChatSession session) {
        roomSessions.computeIfAbsent(roomId, key -> ConcurrentHashMap.newKeySet()).add(session);
    }

    public void leaveRoom(String roomId, ChatSession session) {
        Set<ChatSession> members = roomSessions.get(roomId);
        if (null != members) {
            members.remove(session);
        }
    }

    public void unregister(ChatSession session) {
        sessions.remove(session.id());
        roomSessions.values().forEach(members -> members.remove(session));
        String userId = session.userId();
        if (null != userId) {
            userSessions.remove(userId);
        }
    }

    @Override
    public void broadcastChat(ChatMessage message) {
        broadcast(message.getRoomId(), ResponseFactory.broadcastChat(message));
    }

    @Override
    public void broadcastJoin(RoomNotice notice, UserInfo user) {
        broadcast(notice.roomId(), ResponseFactory.joinNotice(notice.roomId(), user));
    }

    @Override
    public void broadcastLeave(RoomNotice notice, UserInfo user) {
        broadcast(notice.roomId(), ResponseFactory.leaveNotice(notice.roomId(), user));
    }

    @Override
    public void broadcastOffline(RoomNotice notice, UserInfo user) {
        broadcast(notice.roomId(), ResponseFactory.offlineNotice(notice.roomId(), user));
    }

    @Override
    public void broadcastRecall(RecallInfo info) {
        broadcast(info.roomId(), ResponseFactory.recallNotify(info));
    }

    @Override
    public void broadcastAi(ChatMessage message, int status) {
        broadcast(message.getRoomId(), ResponseFactory.aiReply(message, status));
    }

    private void broadcast(String roomId, GeneratedMessageV3 message) {
        Set<ChatSession> members = roomSessions.get(roomId);
        if (null == members) {
            return;
        }
        for (ChatSession session : members) {
            if (session.isActive()) {
                codec.send(session.channel(), message);
            }
        }
    }
}
