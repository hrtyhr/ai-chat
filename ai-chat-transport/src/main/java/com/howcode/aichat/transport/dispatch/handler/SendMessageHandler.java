package com.howcode.aichat.transport.dispatch.handler;

import com.howcode.aichat.core.domain.error.ErrorCode;
import com.howcode.aichat.core.port.in.ChatUseCase;
import com.howcode.aichat.transport.dispatch.AbstractCommandHandler;
import com.howcode.aichat.transport.dispatch.ResponseFactory;
import com.howcode.aichat.transport.protocol.AIChatMsgProtocol;
import com.howcode.aichat.transport.session.CommandContext;
import org.springframework.stereotype.Component;

/** 发送消息。 */
@Component
public class SendMessageHandler extends AbstractCommandHandler<AIChatMsgProtocol.SendMsgCmd> {

    private final ChatUseCase chatUseCase;

    public SendMessageHandler(ChatUseCase chatUseCase) {
        this.chatUseCase = chatUseCase;
    }

    @Override
    public Class<AIChatMsgProtocol.SendMsgCmd> commandType() {
        return AIChatMsgProtocol.SendMsgCmd.class;
    }

    @Override
    public void handle(CommandContext context, AIChatMsgProtocol.SendMsgCmd command) {
        String userId = verifyLogin(context);
        if (null == userId) {
            return;
        }
        String roomId = verifyJoinRoom(context);
        if (null == roomId || !roomId.equals(command.getRoomId())) {
            return;
        }
        if (command.getMsgId().isEmpty()) {
            context.send(ResponseFactory.exception(ErrorCode.MESSAGE_ID_ERROR));
            return;
        }
        chatUseCase.send(new ChatUseCase.SendCommand(command.getMsgId(), roomId, userId,
                command.getMsgType().getNumber(), command.getMsg(), command.getExt()));
        context.send(ResponseFactory.sendAck(roomId, userId, command.getMsgId(), command.getExt()));
    }
}
