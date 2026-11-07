package com.howcode.aichat.core.application;

import com.howcode.aichat.core.domain.error.DomainException;
import com.howcode.aichat.core.domain.error.ErrorCode;
import com.howcode.aichat.core.domain.model.RoomInfo;
import com.howcode.aichat.core.domain.model.RoomNotice;
import com.howcode.aichat.core.domain.model.UserInfo;
import com.howcode.aichat.core.port.in.RoomUseCase;
import com.howcode.aichat.core.port.out.IdGenerator;
import com.howcode.aichat.core.port.out.RoomStore;
import com.howcode.aichat.core.port.out.SessionGateway;
import com.howcode.aichat.core.port.out.UserSessionStore;
import com.howcode.aichat.core.support.ChatConstants;

import java.util.List;

/** 房间用例实现。 */
public class RoomApplicationService implements RoomUseCase {

    private final RoomStore roomStore;
    private final UserSessionStore sessionStore;
    private final IdGenerator idGenerator;
    private final SessionGateway sessionGateway;

    public RoomApplicationService(RoomStore roomStore,
                                  UserSessionStore sessionStore,
                                  IdGenerator idGenerator,
                                  SessionGateway sessionGateway) {
        this.roomStore = roomStore;
        this.sessionStore = sessionStore;
        this.idGenerator = idGenerator;
        this.sessionGateway = sessionGateway;
    }

    @Override
    public RoomInfo createRoom(int roomNo, String roomName, int history, int ai, String creatorId) {
        if (null != roomStore.findRoomId(roomNo)) {
            throw new DomainException(ErrorCode.ROOM_EXIST);
        }
        String roomId = idGenerator.newRoomId();
        RoomInfo room = new RoomInfo();
        room.setRoomId(roomId);
        room.setRoomNo(roomNo);
        room.setRoomName(null == roomName ? "" : roomName);
        room.setRoomType(ChatConstants.ROOM_TYPE_NORMAL);
        room.setHistory(history);
        room.setAi(ai);
        roomStore.saveRoom(room);
        roomStore.saveRoomNo(roomNo, roomId);

        UserInfo creator = sessionStore.find(creatorId);
        if (null != creator) {
            creator.setRoomId(roomId);
            creator.setJoinRoomTime(System.currentTimeMillis());
            sessionStore.save(creator);
            roomStore.addMember(roomId, creator);
        }
        room.setMembers(roomStore.findMembers(roomId));
        return room;
    }

    @Override
    public RoomInfo joinRoom(int roomNo, String userId) {
        String roomId = roomStore.findRoomId(roomNo);
        if (null == roomId) {
            throw new DomainException(ErrorCode.ROOM_NOT_EXIST);
        }
        RoomInfo room = roomStore.findRoom(roomId);
        UserInfo user = sessionStore.find(userId);
        if (null == user) {
            throw new DomainException(ErrorCode.USER_NOT_FOUND);
        }
        user.setRoomId(roomId);
        user.setJoinRoomTime(System.currentTimeMillis());
        sessionStore.save(user);
        roomStore.addMember(roomId, user);
        sessionGateway.broadcastJoin(new RoomNotice(roomId, userId), user);
        room.setMembers(roomStore.findMembers(roomId));
        return room;
    }

    @Override
    public void leaveRoom(String roomId, String userId) {
        RoomInfo room = roomStore.findRoom(roomId);
        if (null == room) {
            throw new DomainException(ErrorCode.ROOM_NOT_EXIST);
        }
        UserInfo user = sessionStore.find(userId);
        roomStore.removeMember(roomId, userId);
        if (null != user) {
            user.setRoomId(null);
            user.setJoinRoomTime(null);
            sessionStore.save(user);
        }
        sessionGateway.broadcastLeave(new RoomNotice(roomId, userId), user);
        if (roomStore.findMembers(roomId).isEmpty()) {
            roomStore.removeRoom(roomId);
        }
    }

    @Override
    public List<UserInfo> members(String roomId, String userId) {
        return roomStore.findMembers(roomId);
    }
}
