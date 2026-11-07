package com.howcode.aichat.core.domain.error;

/** 领域错误码。 */
public enum ErrorCode {
    UNKNOWN(10000, "未知错误"),
    USER_NOT_LOGIN(10001, "用户未登录"),
    ROOM_EXIST(10002, "房间已存在"),
    ROOM_NOT_EXIST(10003, "房间不存在"),
    MSG_TYPE_NOT_EXIST(10005, "消息类型不存在"),
    USER_NOT_FOUND(10006, "用户不存在"),
    USER_ALREADY_IN_ROOM(10009, "用户已在房间中"),
    USER_NOT_IN_ROOM(10010, "用户不在房间中"),
    MESSAGE_ID_ERROR(10011, "消息Id错误");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int code() {
        return code;
    }

    public String message() {
        return message;
    }
}
