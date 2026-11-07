package com.howcode.aichat.transport.dispatch.handler;

import com.howcode.aichat.core.port.in.UserUseCase;
import com.howcode.aichat.transport.dispatch.AbstractCommandHandler;
import com.howcode.aichat.transport.dispatch.ResponseFactory;
import com.howcode.aichat.transport.protocol.AIChatMsgProtocol;
import com.howcode.aichat.transport.session.CommandContext;
import com.howcode.aichat.transport.session.SessionRegistry;
import org.springframework.stereotype.Component;

/** 退出登录。 */
@Component
public class LogoutHandler extends AbstractCommandHandler<AIChatMsgProtocol.UserLogoutCmd> {

    private final UserUseCase userUseCase;
    private final SessionRegistry sessionRegistry;

    public LogoutHandler(UserUseCase userUseCase, SessionRegistry sessionRegistry) {
        this.userUseCase = userUseCase;
        this.sessionRegistry = sessionRegistry;
    }

    @Override
    public Class<AIChatMsgProtocol.UserLogoutCmd> commandType() {
        return AIChatMsgProtocol.UserLogoutCmd.class;
    }

    @Override
    public void handle(CommandContext context, AIChatMsgProtocol.UserLogoutCmd command) {
        String userId = verifyLogin(context);
        if (null == userId) {
            return;
        }
        userUseCase.logout(userId);
        sessionRegistry.unbind(userId);
        context.clearRoom();
        context.send(ResponseFactory.logoutAck(userId));
    }
}
