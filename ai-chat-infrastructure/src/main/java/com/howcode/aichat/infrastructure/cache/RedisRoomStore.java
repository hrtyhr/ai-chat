package com.howcode.aichat.infrastructure.cache;

import com.howcode.aichat.core.domain.model.RoomInfo;
import com.howcode.aichat.core.domain.model.UserInfo;
import com.howcode.aichat.core.port.out.RoomStore;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Redis 房间/成员/消息幂等适配器。 */
@Component
public class RedisRoomStore implements RoomStore {

    private static final String INFO = "aichat:room:info:";
    private static final String MEMBERS = "aichat:room:members:";
    private static final String NO = "aichat:room:no:";
    private static final String MSG_IDS = "aichat:room:msgids:";

    @Resource
    private RedisClient redisClient;

    @Override
    public void saveRoom(RoomInfo room) {
        redisClient.set(INFO + room.getRoomId(), room);
    }

    @Override
    public void removeRoom(String roomId) {
        RoomInfo room = findRoom(roomId);
        redisClient.delete(INFO + roomId);
        redisClient.delete(MEMBERS + roomId);
        redisClient.delete(MSG_IDS + roomId);
        if (null != room && null != room.getRoomNo()) {
            redisClient.delete(NO + room.getRoomNo());
        }
    }

    @Override
    public void saveRoomNo(int roomNo, String roomId) {
        redisClient.set(NO + roomNo, roomId);
    }

    @Override
    public String findRoomId(int roomNo) {
        return redisClient.get(NO + roomNo, String.class);
    }

    @Override
    public RoomInfo findRoom(String roomId) {
        return null == roomId ? null : redisClient.get(INFO + roomId, RoomInfo.class);
    }

    @Override
    public void addMember(String roomId, UserInfo user) {
        Map<String, UserInfo> members = loadMembers(roomId);
        members.put(user.getUserId(), user);
        redisClient.set(MEMBERS + roomId, members);
    }

    @Override
    public void removeMember(String roomId, String userId) {
        Map<String, UserInfo> members = loadMembers(roomId);
        if (null != members.remove(userId)) {
            redisClient.set(MEMBERS + roomId, members);
        }
    }

    @Override
    public List<UserInfo> findMembers(String roomId) {
        return new ArrayList<>(loadMembers(roomId).values());
    }

    @Override
    public void markMessage(String roomId, String messageId) {
        redisClient.sAdd(MSG_IDS + roomId, messageId);
    }

    @Override
    public boolean isMessageMarked(String roomId, String messageId) {
        return redisClient.sIsMember(MSG_IDS + roomId, messageId);
    }

    @SuppressWarnings("unchecked")
    private Map<String, UserInfo> loadMembers(String roomId) {
        Object cached = redisClient.get(MEMBERS + roomId);
        if (cached instanceof Map) {
            return (Map<String, UserInfo>) cached;
        }
        return new HashMap<>();
    }
}
