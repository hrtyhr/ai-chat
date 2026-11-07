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

/** 创建房间。 */
@Component
public class CreateRoomHandler extends AbstractCommandHandler<AIChatMsgProtocol.CreateRoomCmd> {

    private final RoomUseCase roomUseCase;
    private final SessionRegistry sessionRegistry;

    public CreateRoomHandler(RoomUseCase roomUseCase, SessionRegistry sessionRegistry) {
        this.roomUseCase = roomUseCase;
        this.sessionRegistry = sessionRegistry;
    }

    @Override
    public Class<AIChatMsgProtocol.CreateRoomCmd> commandType() {
        return AIChatMsgProtocol.CreateRoomCmd.class;
    }

    @Override
    public void handle(CommandContext context, AIChatMsgProtocol.CreateRoomCmd command) {
        String userId = verifyLogin(context);
        if (null == userId) {
            return;
        }
        if (null != context.roomId()) {
            context.send(ResponseFactory.exception(ErrorCode.USER_ALREADY_IN_ROOM));
            return;
        }
        RoomInfo room = roomUseCase.createRoom(command.getRoomNo(), command.getRoomName(),
                command.getHistory(), command.getAi(), userId);
        context.bindRoom(room.getRoomId());
        sessionRegistry.joinRoom(room.getRoomId(), context.session());
        context.send(ResponseFactory.createRoomAck(room));
    }
}
