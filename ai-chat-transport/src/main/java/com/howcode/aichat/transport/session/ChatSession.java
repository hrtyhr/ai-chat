package com.howcode.aichat.transport.session;

import io.netty.channel.Channel;
import io.netty.util.AttributeKey;

/** 对 Netty Channel 的轻量封装：连接即会话，属性挂在 Channel 上。 */
public class ChatSession {

    public static final String USER_ID = "userId";
    public static final String ROOM_ID = "roomId";

    private final Channel channel;

    public ChatSession(Channel channel) {
        this.channel = channel;
    }

    public String id() {
        return channel.id().asLongText();
    }

    public Channel channel() {
        return channel;
    }

    public String userId() {
        return attribute(USER_ID);
    }

    public String roomId() {
        return attribute(ROOM_ID);
    }

    public void bindUser(String userId) {
        setAttribute(USER_ID, userId);
    }

    public void bindRoom(String roomId) {
        setAttribute(ROOM_ID, roomId);
    }

    public void clearRoom() {
        removeAttribute(ROOM_ID);
    }

    public String attribute(String key) {
        Object value = channel.attr(AttributeKey.valueOf(key)).get();
        return null == value ? null : value.toString();
    }

    public void setAttribute(String key, Object value) {
        channel.attr(AttributeKey.valueOf(key)).set(value);
    }

    public void removeAttribute(String key) {
        channel.attr(AttributeKey.valueOf(key)).set(null);
    }

    public boolean isActive() {
        return channel.isActive();
    }

    public void close() {
        channel.close();
    }
}
