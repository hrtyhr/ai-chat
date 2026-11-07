package com.howcode.aichat.transport.dispatch;

import com.google.protobuf.GeneratedMessageV3;
import com.howcode.aichat.core.port.in.UserUseCase;
import com.howcode.aichat.transport.protocol.ProtocolCodec;
import com.howcode.aichat.transport.session.ChatSession;
import com.howcode.aichat.transport.session.CommandContext;
import com.howcode.aichat.transport.session.SessionRegistry;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** Netty 接入适配器：二进制帧 -> Protobuf -> 命令总线。 */
@Component
@ChannelHandler.Sharable
public class ChatWebSocketHandler extends SimpleChannelInboundHandler<BinaryWebSocketFrame> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChatWebSocketHandler.class);

    private final ProtocolCodec protocolCodec;
    private final CommandBus commandBus;
    private final SessionRegistry sessionRegistry;
    private final UserUseCase userUseCase;

    public ChatWebSocketHandler(ProtocolCodec protocolCodec,
                                CommandBus commandBus,
                                SessionRegistry sessionRegistry,
                                UserUseCase userUseCase) {
        this.protocolCodec = protocolCodec;
        this.commandBus = commandBus;
        this.sessionRegistry = sessionRegistry;
        this.userUseCase = userUseCase;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        sessionRegistry.register(new ChatSession(ctx.channel()));
        super.channelActive(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, BinaryWebSocketFrame frame) {
        try {
            byte[] bytes = ByteBufUtil.getBytes(frame.content());
            GeneratedMessageV3 command = protocolCodec.decode(bytes);
            if (null == command) {
                return;
            }
            ChatSession session = sessionRegistry.byChannelId(ctx.channel().id().asLongText());
            if (null == session) {
                return;
            }
            commandBus.dispatch(new CommandContext(session, protocolCodec), command);
        } catch (Exception e) {
            LOGGER.error("消息处理失败", e);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ChatSession session = sessionRegistry.byChannelId(ctx.channel().id().asLongText());
        if (null != session) {
            String userId = session.userId();
            if (null != userId) {
                userUseCase.offline(userId);
                sessionRegistry.unbind(userId);
            }
            sessionRegistry.unregister(session);
        }
        super.channelInactive(ctx);
    }
}
