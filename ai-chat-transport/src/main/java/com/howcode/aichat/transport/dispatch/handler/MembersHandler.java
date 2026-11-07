package com.howcode.aichat.transport.dispatch.handler;

import com.howcode.aichat.core.port.in.RoomUseCase;
import com.howcode.aichat.transport.dispatch.AbstractCommandHandler;
import com.howcode.aichat.transport.dispatch.ResponseFactory;
import com.howcode.aichat.transport.protocol.AIChatMsgProtocol;
import com.howcode.aichat.transport.session.CommandContext;
import org.springframework.stereotype.Component;

/** 同步房间成员。 */
@Component
public class MembersHandler extends AbstractCommandHandler<AIChatMsgProtocol.SyncRoomMembersCmd> {

    private final RoomUseCase roomUseCase;

    public MembersHandler(RoomUseCase roomUseCase) {
        this.roomUseCase = roomUseCase;
    }

    @Override
    public Class<AIChatMsgProtocol.SyncRoomMembersCmd> commandType() {
        return AIChatMsgProtocol.SyncRoomMembersCmd.class;
    }

    @Override
    public void handle(CommandContext context, AIChatMsgProtocol.SyncRoomMembersCmd command) {
        String userId = verifyLogin(context);
        if (null == userId) {
            return;
        }
        String roomId = verifyJoinRoom(context);
        if (null == roomId || !roomId.equals(command.getRoomId())) {
            return;
        }
        context.send(ResponseFactory.membersAck(roomId, roomUseCase.members(roomId, userId)));
    }
}
