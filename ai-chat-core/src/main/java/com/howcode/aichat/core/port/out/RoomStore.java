package com.howcode.aichat.core.port.out;

import com.howcode.aichat.core.domain.model.RoomInfo;
import com.howcode.aichat.core.domain.model.UserInfo;

import java.util.List;

/** 出站端口：房间与成员、消息幂等（Redis）。 */
public interface RoomStore {
    void saveRoom(RoomInfo room);

    void removeRoom(String roomId);

    void saveRoomNo(int roomNo, String roomId);

    String findRoomId(int roomNo);

    RoomInfo findRoom(String roomId);

    void addMember(String roomId, UserInfo user);

    void removeMember(String roomId, String userId);

    List<UserInfo> findMembers(String roomId);

    void markMessage(String roomId, String messageId);

    boolean isMessageMarked(String roomId, String messageId);
}
