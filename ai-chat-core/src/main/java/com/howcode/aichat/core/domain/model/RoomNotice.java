package com.howcode.aichat.core.domain.model;

/** 房间成员变动 / 离线事件载荷。 */
public record RoomNotice(String roomId, String userId) {
}
