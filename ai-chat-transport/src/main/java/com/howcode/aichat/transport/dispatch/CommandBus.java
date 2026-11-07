package com.howcode.aichat.transport.dispatch;

import com.google.protobuf.GeneratedMessageV3;
import com.howcode.aichat.core.domain.error.DomainException;
import com.howcode.aichat.core.domain.error.ErrorCode;
import com.howcode.aichat.transport.session.CommandContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 命令总线：构造时收集所有 CommandHandler，按显式声明的命令类型建立路由表。
 * 相比反射扫描，显式声明更直观、编译期可校验。
 */
@Component
public class CommandBus {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommandBus.class);

    private final Map<Class<?>, CommandHandler<?>> handlers = new HashMap<>();

    public CommandBus(List<CommandHandler<?>> handlerList) {
        for (CommandHandler<?> handler : handlerList) {
            handlers.put(handler.commandType(), handler);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public void dispatch(CommandContext context, GeneratedMessageV3 command) {
        CommandHandler handler = handlers.get(command.getClass());
        if (null == handler) {
            context.send(ResponseFactory.exception(ErrorCode.UNKNOWN));
            return;
        }
        try {
            handler.handle(context, command);
        } catch (DomainException e) {
            context.send(ResponseFactory.exception(e.getErrorCode()));
        } catch (Exception e) {
            LOGGER.error("指令处理失败: {}", command.getClass().getSimpleName(), e);
            context.send(ResponseFactory.exception(ErrorCode.UNKNOWN));
        }
    }
}
