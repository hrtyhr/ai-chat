package com.howcode.aichat.infrastructure.ai;

import com.howcode.aichat.core.port.out.AiGateway;

/** 本地 Mock：未配置 apiKey 时使用，便于离线联调。 */
public class MockAiGateway implements AiGateway {

    @Override
    public String chat(String userId, String userName, String prompt) {
        String who = (null == userName || userName.isEmpty()) ? "朋友" : userName;
        return "【模拟AI】" + who + "，我收到了你的消息：「" + prompt
                + "」。配置 aichat.ai.api-key 后可接入真实大模型。";
    }
}
