package com.howcode.aichat.core.domain.enums;

/** 房间类型。 */
public enum RoomKind {
    NORMAL(1), AI(2);

    private final int code;

    RoomKind(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }
}
