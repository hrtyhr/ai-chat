package com.howcode.aichat.core.application;

import com.howcode.aichat.core.domain.enums.MessageType;
import com.howcode.aichat.core.domain.enums.Toggle;
import com.howcode.aichat.core.domain.error.DomainException;
import com.howcode.aichat.core.domain.error.ErrorCode;
import com.howcode.aichat.core.domain.model.ChatMessage;
import com.howcode.aichat.core.domain.model.RecallInfo;
import com.howcode.aichat.core.domain.model.RoomInfo;
import com.howcode.aichat.core.domain.model.UserInfo;
import com.howcode.aichat.core.port.in.ChatUseCase;
import com.howcode.aichat.core.port.out.AiGateway;
import com.howcode.aichat.core.port.out.IdGenerator;
import com.howcode.aichat.core.port.out.MessagePublisher;
import com.howcode.aichat.core.port.out.MessageRepository;
import com.howcode.aichat.core.port.out.RoomStore;
import com.howcode.aichat.core.port.out.SessionGateway;
import com.howcode.aichat.core.port.out.UserSessionStore;
import com.howcode.aichat.core.support.ChatConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.Executor;

/** 消息用例实现：幂等、异步广播/落库、AI 回复。 */
public class ChatApplicationService implements ChatUseCase {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChatApplicationService.class);

    private final RoomStore roomStore;
    private final UserSessionStore sessionStore;
    private final MessageRepository messageRepository;
    private final IdGenerator idGenerator;
    private final MessagePublisher messagePublisher;
    private final SessionGateway sessionGateway;
    private final AiGateway aiGateway;
    private final Executor executor;

    public ChatApplicationService(RoomStore roomStore,
                                  UserSessionStore sessionStore,
                                  MessageRepository messageRepository,
                                  IdGenerator idGenerator,
                                  MessagePublisher messagePublisher,
                                  SessionGateway sessionGateway,
                                  AiGateway aiGateway,
                                  Executor executor) {
        this.roomStore = roomStore;
        this.sessionStore = sessionStore;
        this.messageRepository = messageRepository;
        this.idGenerator = idGenerator;
        this.messagePublisher = messagePublisher;
        this.sessionGateway = sessionGateway;
        this.aiGateway = aiGateway;
        this.executor = executor;
    }

    @Override
    public void send(SendCommand command) {
        RoomInfo room = roomStore.findRoom(command.roomId());
        if (null == room) {
            throw new DomainException(ErrorCode.ROOM_NOT_EXIST);
        }
        if (roomStore.isMessageMarked(command.roomId(), command.messageId())) {
            return;
        }
        UserInfo sender = sessionStore.find(command.senderId());
        if (null == sender) {
            throw new DomainException(ErrorCode.USER_NOT_FOUND);
        }
        ChatMessage message = new ChatMessage();
        message.setMessageId(command.messageId());
        message.setRoomId(command.roomId());
        message.setSenderId(command.senderId());
        message.setSenderName(sender.getUserName());
        message.setSenderAvatar(sender.getUserAvatar());
        message.setMessageType(command.messageType());
        message.setContent(command.content());
        message.setExt(command.ext());
        message.setCreateTime(System.currentTimeMillis());

        messagePublisher.publishBroadcast(message);
        messagePublisher.publishStore(message);
        roomStore.markMessage(command.roomId(), command.messageId());

        if (shouldTriggerAi(room, message)) {
            triggerAi(room, sender, message);
        }
    }

    @Override
    public List<ChatMessage> history(String roomId, String userId) {
        return messageRepository.findHistory(roomId, ChatConstants.HISTORY_LIMIT);
    }

    @Override
    public void recall(String roomId, String messageId, String userId) {
        sessionGateway.broadcastRecall(new RecallInfo(roomId, messageId, userId));
    }

    private boolean shouldTriggerAi(RoomInfo room, ChatMessage message) {
        if (message.getMessageType() != MessageType.TEXT.code()) {
            return false;
        }
        boolean roomAiOpen = null != room.getAi() && room.getAi() == Toggle.OPEN.code();
        boolean atAi = null != message.getContent()
                && message.getContent().toUpperCase().contains(ChatConstants.AI_TRIGGER);
        return roomAiOpen || atAi;
    }

    private void triggerAi(RoomInfo room, UserInfo sender, ChatMessage origin) {
        executor.execute(() -> {
            String reply;
            int status = 1;
            try {
                reply = aiGateway.chat(sender.getUserId(), sender.getUserName(), origin.getContent());
            } catch (Exception e) {
                LOGGER.error("AI 回复失败", e);
                reply = "抱歉，AI 服务暂时不可用，请稍后再试。";
                status = 0;
            }
            ChatMessage aiMessage = new ChatMessage();
            aiMessage.setMessageId(idGenerator.newMessageId());
            aiMessage.setRoomId(room.getRoomId());
            aiMessage.setSenderId(ChatConstants.AI_USER_ID);
            aiMessage.setSenderName(ChatConstants.AI_USER_NAME);
            aiMessage.setSenderAvatar(ChatConstants.AI_USER_AVATAR);
            aiMessage.setMessageType(MessageType.TEXT.code());
            aiMessage.setContent(reply);
            aiMessage.setExt("");
            aiMessage.setCreateTime(System.currentTimeMillis());
            sessionGateway.broadcastAi(aiMessage, status);
            messagePublisher.publishStore(aiMessage);
        });
    }
}
