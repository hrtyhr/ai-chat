package com.howcode.aichat.transport.dispatch;

import com.google.protobuf.GeneratedMessageV3;
import com.howcode.aichat.core.domain.error.ErrorCode;
import com.howcode.aichat.transport.session.CommandContext;

/** 指令处理器基类：抽取登录/房间前置校验。 */
public abstract class AbstractCommandHandler<T extends GeneratedMessageV3> implements CommandHandler<T> {

    protected String verifyLogin(CommandContext context) {
        String userId = context.userId();
        if (null == userId) {
            context.send(ResponseFactory.exception(ErrorCode.USER_NOT_LOGIN));
            return null;
        }
        return userId;
    }

    protected String verifyJoinRoom(CommandContext context) {
        String roomId = context.roomId();
        if (null == roomId) {
            context.send(ResponseFactory.exception(ErrorCode.USER_NOT_IN_ROOM));
            return null;
        }
        return roomId;
    }
}
