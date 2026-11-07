package com.howcode.aichat.transport.dispatch.handler;

import com.howcode.aichat.core.domain.model.UserInfo;
import com.howcode.aichat.core.port.in.UserUseCase;
import com.howcode.aichat.transport.dispatch.AbstractCommandHandler;
import com.howcode.aichat.transport.dispatch.ResponseFactory;
import com.howcode.aichat.transport.protocol.AIChatMsgProtocol;
import com.howcode.aichat.transport.session.CommandContext;
import com.howcode.aichat.transport.session.SessionRegistry;
import org.springframework.stereotype.Component;

/** 登录（匿名建号）。 */
@Component
public class LoginHandler extends AbstractCommandHandler<AIChatMsgProtocol.UserLoginCmd> {

    private final UserUseCase userUseCase;
    private final SessionRegistry sessionRegistry;

    public LoginHandler(UserUseCase userUseCase, SessionRegistry sessionRegistry) {
        this.userUseCase = userUseCase;
        this.sessionRegistry = sessionRegistry;
    }

    @Override
    public Class<AIChatMsgProtocol.UserLoginCmd> commandType() {
        return AIChatMsgProtocol.UserLoginCmd.class;
    }

    @Override
    public void handle(CommandContext context, AIChatMsgProtocol.UserLoginCmd command) {
        UserInfo user = userUseCase.login(command.getUserName(), command.getUserAvatar());
        context.bindUser(user.getUserId());
        sessionRegistry.bind(user.getUserId(), context.session());
        context.send(ResponseFactory.loginAck(user));
    }
}
