package com.howcode.aichat.transport.dispatch.handler;

import com.howcode.aichat.core.port.in.ChatUseCase;
import com.howcode.aichat.transport.dispatch.AbstractCommandHandler;
import com.howcode.aichat.transport.dispatch.ResponseFactory;
import com.howcode.aichat.transport.protocol.AIChatMsgProtocol;
import com.howcode.aichat.transport.session.CommandContext;
import org.springframework.stereotype.Component;

/** 撤回消息。 */
@Component
public class RecallHandler extends AbstractCommandHandler<AIChatMsgProtocol.RecallMsgCmd> {

    private final ChatUseCase chatUseCase;

    public RecallHandler(ChatUseCase chatUseCase) {
        this.chatUseCase = chatUseCase;
    }

    @Override
    public Class<AIChatMsgProtocol.RecallMsgCmd> commandType() {
        return AIChatMsgProtocol.RecallMsgCmd.class;
    }

    @Override
    public void handle(CommandContext context, AIChatMsgProtocol.RecallMsgCmd command) {
        String userId = verifyLogin(context);
        if (null == userId) {
            return;
        }
        String roomId = verifyJoinRoom(context);
        if (null == roomId || !roomId.equals(command.getRoomId())) {
            return;
        }
        chatUseCase.recall(roomId, command.getMsgId(), userId);
        context.send(ResponseFactory.recallAck(roomId, command.getMsgId(), userId));
    }
}
