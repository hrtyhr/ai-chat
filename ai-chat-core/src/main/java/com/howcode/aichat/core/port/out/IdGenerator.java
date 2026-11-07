package com.howcode.aichat.core.port.out;

/** 出站端口：分布式 ID 生成。 */
public interface IdGenerator {
    String newUserId();

    String newRoomId();

    String newMessageId();
}
