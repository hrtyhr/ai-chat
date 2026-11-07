package com.howcode.aichat.infrastructure.support;

import cn.hutool.core.util.IdUtil;
import com.howcode.aichat.core.port.out.IdGenerator;
import org.springframework.stereotype.Component;

/** 基于 Hutool 雪花算法的 ID 生成器。 */
@Component
public class SnowflakeIdGenerator implements IdGenerator {

    private static final String USER_PREFIX = "aichat_user_";
    private static final String ROOM_PREFIX = "aichat_room_";

    @Override
    public String newUserId() {
        return USER_PREFIX + IdUtil.getSnowflakeNextIdStr();
    }

    @Override
    public String newRoomId() {
        return ROOM_PREFIX + IdUtil.getSnowflakeNextIdStr();
    }

    @Override
    public String newMessageId() {
        return IdUtil.getSnowflakeNextIdStr();
    }
}
