package com.howcode.aichat.transport.dispatch.handler;

import com.howcode.aichat.core.port.in.ChatUseCase;
import com.howcode.aichat.transport.dispatch.AbstractCommandHandler;
import com.howcode.aichat.transport.dispatch.ResponseFactory;
import com.howcode.aichat.transport.protocol.AIChatMsgProtocol;
import com.howcode.aichat.transport.session.CommandContext;
import org.springframework.stereotype.Component;

/** 同步聊天记录。 */
@Component
public class HistoryHandler extends AbstractCommandHandler<AIChatMsgProtocol.SyncChatRecordCmd> {

    private final ChatUseCase chatUseCase;

    public HistoryHandler(ChatUseCase chatUseCase) {
        this.chatUseCase = chatUseCase;
    }

    @Override
    public Class<AIChatMsgProtocol.SyncChatRecordCmd> commandType() {
        return AIChatMsgProtocol.SyncChatRecordCmd.class;
    }

    @Override
    public void handle(CommandContext context, AIChatMsgProtocol.SyncChatRecordCmd command) {
        String userId = verifyLogin(context);
        if (null == userId) {
            return;
        }
        String roomId = verifyJoinRoom(context);
        if (null == roomId || !roomId.equals(command.getRoomId())) {
            return;
        }
        context.send(ResponseFactory.historyAck(roomId, chatUseCase.history(roomId, userId)));
    }
}
