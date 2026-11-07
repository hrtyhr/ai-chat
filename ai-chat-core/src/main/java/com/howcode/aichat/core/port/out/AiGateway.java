package com.howcode.aichat.core.port.out;

/** 出站端口：大模型调用。 */
public interface AiGateway {
    String chat(String userId, String userName, String prompt);
}
