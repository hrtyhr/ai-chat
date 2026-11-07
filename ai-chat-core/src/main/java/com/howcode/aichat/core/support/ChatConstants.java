package com.howcode.aichat.core.support;

/** 领域常量。 */
public final class ChatConstants {

    public static final String AI_USER_ID = "aichat_ai";
    public static final String AI_USER_NAME = "小Q";
    public static final String AI_USER_AVATAR = "";
    public static final String AI_TRIGGER = "@AI";

    public static final long ONLINE_TTL_SECONDS = 24 * 60 * 60L;
    public static final long OFFLINE_TTL_SECONDS = 9 * 60L;
    public static final int HISTORY_LIMIT = 200;

    public static final int ROOM_TYPE_NORMAL = 1;
    public static final int ROOM_TYPE_AI = 2;

    private ChatConstants() {
    }
}
