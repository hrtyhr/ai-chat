package com.howcode.aichat.core.domain.model;

import lombok.Data;

/**
 * 聊天消息领域模型（实时消息与历史记录共用）。
 */
@Data
public class ChatMessage {
    private String messageId;
    private String roomId;
    private String senderId;
    private String senderName;
    private String senderAvatar;
    /** 消息类型，见 MessageType */
    private Integer messageType;
    private String content;
    private String ext;
    /** 创建时间（epoch millis） */
    private Long createTime;
}
