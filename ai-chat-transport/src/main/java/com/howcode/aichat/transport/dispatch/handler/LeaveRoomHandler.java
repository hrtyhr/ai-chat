package com.howcode.aichat.transport.dispatch.handler;

import com.howcode.aichat.core.port.in.RoomUseCase;
import com.howcode.aichat.transport.dispatch.AbstractCommandHandler;
import com.howcode.aichat.transport.dispatch.ResponseFactory;
import com.howcode.aichat.transport.protocol.AIChatMsgProtocol;
import com.howcode.aichat.transport.session.CommandContext;
import com.howcode.aichat.transport.session.SessionRegistry;
import org.springframework.stereotype.Component;

/** 离开房间。 */
@Component
public class LeaveRoomHandler extends AbstractCommandHandler<AIChatMsgProtocol.LeaveRoomCmd> {

    private final RoomUseCase roomUseCase;
    private final SessionRegistry sessionRegistry;

    public LeaveRoomHandler(RoomUseCase roomUseCase, SessionRegistry sessionRegistry) {
        this.roomUseCase = roomUseCase;
        this.sessionRegistry = sessionRegistry;
    }

    @Override
    public Class<AIChatMsgProtocol.LeaveRoomCmd> commandType() {
        return AIChatMsgProtocol.LeaveRoomCmd.class;
    }

    @Override
    public void handle(CommandContext context, AIChatMsgProtocol.LeaveRoomCmd command) {
        String userId = verifyLogin(context);
        if (null == userId) {
            return;
        }
        String roomId = verifyJoinRoom(context);
        if (null == roomId || !roomId.equals(command.getRoomId())) {
            return;
        }
        roomUseCase.leaveRoom(roomId, userId);
        sessionRegistry.leaveRoom(roomId, context.session());
        context.clearRoom();
        context.send(ResponseFactory.leaveRoomAck(roomId));
    }
}
