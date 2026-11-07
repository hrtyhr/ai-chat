package com.howcode.aichat.transport.protocol;

import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.Message;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 协议识别器：建立「命令号 <-> 消息类」双向映射。
 * 约定：枚举名去下划线小写 == 消息类名小写。
 */
@Component
public class ProtocolRecognizer implements InitializingBean {

    private final Map<Integer, GeneratedMessageV3> byCommand = new HashMap<>();
    private final Map<Class<?>, Integer> byClass = new HashMap<>();

    public void init() {
        for (Class<?> inner : AIChatMsgProtocol.class.getDeclaredClasses()) {
            if (!GeneratedMessageV3.class.isAssignableFrom(inner)) {
                continue;
            }
            String className = inner.getSimpleName().toLowerCase();
            for (AIChatMsgProtocol.MsgCommand command : AIChatMsgProtocol.MsgCommand.values()) {
                String commandName = command.name().replace("_", "").toLowerCase();
                if (!commandName.equals(className)) {
                    continue;
                }
                try {
                    Object defaultInstance = inner.getDeclaredMethod("getDefaultInstance").invoke(inner);
                    byCommand.put(command.getNumber(), (GeneratedMessageV3) defaultInstance);
                    byClass.put(inner, command.getNumber());
                } catch (Exception ignored) {
                    // 无法反射的消息类型跳过
                }
            }
        }
    }

    public Message.Builder builderOf(int command) {
        GeneratedMessageV3 proto = byCommand.get(command);
        return null == proto ? null : proto.newBuilderForType();
    }

    public int commandOf(Class<?> clazz) {
        Integer command = byClass.get(clazz);
        return null == command ? -1 : command;
    }

    @Override
    public void afterPropertiesSet() {
        init();
    }
}
