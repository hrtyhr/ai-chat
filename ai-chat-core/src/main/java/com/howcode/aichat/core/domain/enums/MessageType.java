package com.howcode.aichat.core.domain.enums;

/** 消息类型。 */
public enum MessageType {
    TEXT(0), IMAGE(1), VOICE(2), VIDEO(3), FILE(4);

    private final int code;

    MessageType(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }

    public static MessageType from(int code) {
        for (MessageType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        return TEXT;
    }
}
