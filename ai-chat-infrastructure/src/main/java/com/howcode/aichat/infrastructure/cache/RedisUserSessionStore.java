package com.howcode.aichat.infrastructure.cache;

import com.howcode.aichat.core.domain.model.UserInfo;
import com.howcode.aichat.core.port.out.UserSessionStore;
import com.howcode.aichat.core.support.ChatConstants;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/** Redis 在线会话适配器：在线 24h，断线后改为 9min 软下线。 */
@Component
public class RedisUserSessionStore implements UserSessionStore {

    private static final String PREFIX = "aichat:session:user:";

    @Resource
    private RedisClient redisClient;

    private static String key(String userId) {
        return PREFIX + userId;
    }

    @Override
    public void save(UserInfo user) {
        redisClient.set(key(user.getUserId()), user, ChatConstants.ONLINE_TTL_SECONDS, TimeUnit.SECONDS);
    }

    @Override
    public UserInfo find(String userId) {
        return redisClient.get(key(userId), UserInfo.class);
    }

    @Override
    public void remove(String userId) {
        redisClient.delete(key(userId));
    }

    @Override
    public void expire(String userId, long ttlSeconds) {
        redisClient.expire(key(userId), ttlSeconds, TimeUnit.SECONDS);
    }
}
