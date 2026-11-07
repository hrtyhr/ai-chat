package com.howcode.aichat.transport.netty;

import com.howcode.aichat.core.domain.model.UserInfo;
import com.howcode.aichat.core.port.out.UserSessionStore;
import com.howcode.aichat.transport.dispatch.ResponseFactory;
import com.howcode.aichat.transport.protocol.ProtocolCodec;
import com.howcode.aichat.transport.session.ChatSession;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.springframework.stereotype.Component;

/** 空闲检测：超时先下发 OfflineMsg，再关闭连接。 */
@Component
@ChannelHandler.Sharable
public class IdleDetectHandler extends ChannelInboundHandlerAdapter {

    private final ProtocolCodec protocolCodec;
    private final UserSessionStore userSessionStore;

    public IdleDetectHandler(ProtocolCodec protocolCodec, UserSessionStore userSessionStore) {
        this.protocolCodec = protocolCodec;
        this.userSessionStore = userSessionStore;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent event && event.state() == IdleState.ALL_IDLE) {
            ChatSession session = new ChatSession(ctx.channel());
            String userId = session.userId();
            if (null != userId) {
                UserInfo user = userSessionStore.find(userId);
                if (null != user) {
                    protocolCodec.send(ctx.channel(), ResponseFactory.offlineMsg(user, session.roomId()));
                }
            }
            ctx.channel().close();
            return;
        }
        super.userEventTriggered(ctx, evt);
    }
}
