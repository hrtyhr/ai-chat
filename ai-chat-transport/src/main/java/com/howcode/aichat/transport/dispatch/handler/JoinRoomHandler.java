package com.howcode.aichat.transport.dispatch.handler;

import com.howcode.aichat.core.domain.error.ErrorCode;
import com.howcode.aichat.core.domain.model.RoomInfo;
import com.howcode.aichat.core.port.in.RoomUseCase;
import com.howcode.aichat.transport.dispatch.AbstractCommandHandler;
import com.howcode.aichat.transport.dispatch.ResponseFactory;
import com.howcode.aichat.transport.protocol.AIChatMsgProtocol;
import com.howcode.aichat.transport.session.CommandContext;
import com.howcode.aichat.transport.session.SessionRegistry;
import org.springframework.stereotype.Component;

/** 加入房间。 */
@Component
public class JoinRoomHandler extends AbstractCommandHandler<AIChatMsgProtocol.JoinRoomCmd> {

    private final RoomUseCase roomUseCase;
    private final SessionRegistry sessionRegistry;

    public JoinRoomHandler(RoomUseCase roomUseCase, SessionRegistry sessionRegistry) {
        this.roomUseCase = roomUseCase;
        this.sessionRegistry = sessionRegistry;
    }

    @Override
    public Class<AIChatMsgProtocol.JoinRoomCmd> commandType() {
        return AIChatMsgProtocol.JoinRoomCmd.class;
    }

    @Override
    public void handle(CommandContext context, AIChatMsgProtocol.JoinRoomCmd command) {
        String userId = verifyLogin(context);
        if (null == userId) {
            return;
        }
        if (null != context.roomId()) {
            context.send(ResponseFactory.exception(ErrorCode.USER_ALREADY_IN_ROOM));
            return;
        }
        RoomInfo room = roomUseCase.joinRoom(command.getRoomNo(), userId);
        context.bindRoom(room.getRoomId());
        sessionRegistry.joinRoom(room.getRoomId(), context.session());
        context.send(ResponseFactory.joinRoomAck(room));
    }
}
