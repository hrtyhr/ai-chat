package com.howcode.aichat.transport.dispatch.handler;

import com.howcode.aichat.transport.dispatch.AbstractCommandHandler;
import com.howcode.aichat.transport.dispatch.ResponseFactory;
import com.howcode.aichat.transport.protocol.AIChatMsgProtocol;
import com.howcode.aichat.transport.session.CommandContext;
import org.springframework.stereotype.Component;

/** 心跳。 */
@Component
public class HeartbeatHandler extends AbstractCommandHandler<AIChatMsgProtocol.HeartBeatCmd> {

    @Override
    public Class<AIChatMsgProtocol.HeartBeatCmd> commandType() {
        return AIChatMsgProtocol.HeartBeatCmd.class;
    }

    @Override
    public void handle(CommandContext context, AIChatMsgProtocol.HeartBeatCmd command) {
        context.send(ResponseFactory.heartbeatAck());
    }
}
