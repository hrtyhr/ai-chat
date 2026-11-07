package com.howcode.aichat.core.port.in;

import com.howcode.aichat.core.domain.model.RoomInfo;
import com.howcode.aichat.core.domain.model.UserInfo;

import java.util.List;

/** 入站端口：房间用例。 */
public interface RoomUseCase {
    RoomInfo createRoom(int roomNo, String roomName, int history, int ai, String creatorId);

    RoomInfo joinRoom(int roomNo, String userId);

    void leaveRoom(String roomId, String userId);

    List<UserInfo> members(String roomId, String userId);
}
