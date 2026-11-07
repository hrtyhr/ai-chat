package com.howcode.aichat.transport.dispatch;

import com.google.protobuf.GeneratedMessageV3;
import com.howcode.aichat.transport.session.CommandContext;

/** 指令处理器：每个实现显式声明自己负责的命令类型。 */
public interface CommandHandler<T extends GeneratedMessageV3> {
    Class<T> commandType();

    void handle(CommandContext context, T command);
}
