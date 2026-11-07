package com.howcode.aichat.core.port.out;

import com.howcode.aichat.core.domain.model.UserInfo;

/** 出站端口：在线用户会话（Redis，带 TTL）。 */
public interface UserSessionStore {
    void save(UserInfo user);

    UserInfo find(String userId);

    void remove(String userId);

    void expire(String userId, long ttlSeconds);
}
