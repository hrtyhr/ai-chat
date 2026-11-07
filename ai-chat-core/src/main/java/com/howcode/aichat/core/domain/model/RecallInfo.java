package com.howcode.aichat.core.domain.model;

/** 撤回消息事件载荷。 */
public record RecallInfo(String roomId, String messageId, String userId) {
}
