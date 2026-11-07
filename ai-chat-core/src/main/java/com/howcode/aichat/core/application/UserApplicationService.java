package com.howcode.aichat.core.application;

import com.howcode.aichat.core.domain.model.RoomNotice;
import com.howcode.aichat.core.domain.model.UserInfo;
import com.howcode.aichat.core.port.in.UserUseCase;
import com.howcode.aichat.core.port.out.IdGenerator;
import com.howcode.aichat.core.port.out.RoomStore;
import com.howcode.aichat.core.port.out.SessionGateway;
import com.howcode.aichat.core.port.out.UserRepository;
import com.howcode.aichat.core.port.out.UserSessionStore;
import com.howcode.aichat.core.support.ChatConstants;

/** 用户用例实现（纯 Java，无框架依赖）。 */
public class UserApplicationService implements UserUseCase {

    private final UserRepository userRepository;
    private final UserSessionStore sessionStore;
    private final RoomStore roomStore;
    private final IdGenerator idGenerator;
    private final SessionGateway sessionGateway;

    public UserApplicationService(UserRepository userRepository,
                                  UserSessionStore sessionStore,
                                  RoomStore roomStore,
                                  IdGenerator idGenerator,
                                  SessionGateway sessionGateway) {
        this.userRepository = userRepository;
        this.sessionStore = sessionStore;
        this.roomStore = roomStore;
        this.idGenerator = idGenerator;
        this.sessionGateway = sessionGateway;
    }

    @Override
    public UserInfo login(String userName, String userAvatar) {
        UserInfo user = new UserInfo();
        user.setUserId(idGenerator.newUserId());
        user.setUserName(null == userName ? "" : userName);
        user.setUserAvatar(null == userAvatar ? "" : userAvatar);
        userRepository.save(user);
        sessionStore.save(user);
        return user;
    }

    @Override
    public void logout(String userId) {
        UserInfo user = sessionStore.find(userId);
        if (null == user) {
            return;
        }
        String roomId = user.getRoomId();
        if (null != roomId) {
            roomStore.removeMember(roomId, userId);
            sessionGateway.broadcastLeave(new RoomNotice(roomId, userId), user);
            if (roomStore.findMembers(roomId).isEmpty()) {
                roomStore.removeRoom(roomId);
            }
            user.setRoomId(null);
        }
        sessionStore.remove(userId);
    }

    @Override
    public void offline(String userId) {
        UserInfo user = sessionStore.find(userId);
        if (null == user) {
            return;
        }
        sessionStore.expire(userId, ChatConstants.OFFLINE_TTL_SECONDS);
        if (null != user.getRoomId()) {
            sessionGateway.broadcastOffline(new RoomNotice(user.getRoomId(), userId), user);
        }
    }
}
