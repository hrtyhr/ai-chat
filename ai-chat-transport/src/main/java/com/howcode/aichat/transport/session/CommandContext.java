package com.howcode.aichat.transport.session;

import com.google.protobuf.GeneratedMessageV3;
import com.howcode.aichat.transport.protocol.ProtocolCodec;

/** 指令处理上下文：把会话与编码器组合，供 handler 使用。 */
public class CommandContext {

    private final ChatSession session;
    private final ProtocolCodec codec;

    public CommandContext(ChatSession session, ProtocolCodec codec) {
        this.session = session;
        this.codec = codec;
    }

    public ChatSession session() {
        return session;
    }

    public String userId() {
        return session.userId();
    }

    public String roomId() {
        return session.roomId();
    }

    public void bindUser(String userId) {
        session.bindUser(userId);
    }

    public void bindRoom(String roomId) {
        session.bindRoom(roomId);
    }

    public void clearRoom() {
        session.clearRoom();
    }

    public void send(GeneratedMessageV3 message) {
        codec.send(session.channel(), message);
    }

    public void close() {
        session.close();
    }
}
