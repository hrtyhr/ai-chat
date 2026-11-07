package com.howcode.aichat.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.howcode.aichat.core.domain.model.ChatMessage;
import com.howcode.aichat.core.port.out.MessageRepository;
import com.howcode.aichat.infrastructure.persistence.entity.MessageEntity;
import com.howcode.aichat.infrastructure.persistence.entity.UserEntity;
import com.howcode.aichat.infrastructure.persistence.mapper.MessageMapper;
import com.howcode.aichat.infrastructure.persistence.mapper.UserMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/** MySQL 聊天记录仓储适配器。 */
@Component
public class MyBatisMessageRepository implements MessageRepository {

    @Resource
    private MessageMapper messageMapper;
    @Resource
    private UserMapper userMapper;

    @Override
    public void save(ChatMessage message) {
        MessageEntity entity = new MessageEntity();
        entity.setMessageId(message.getMessageId());
        entity.setRoomId(message.getRoomId());
        entity.setSenderId(message.getSenderId());
        entity.setMessageType(null == message.getMessageType() ? 0 : message.getMessageType());
        entity.setMessageContent(null == message.getContent() ? "" : message.getContent());
        entity.setMessageExt(null == message.getExt() ? "" : message.getExt());
        entity.setStatus(1);
        long timestamp = null == message.getCreateTime() ? System.currentTimeMillis() : message.getCreateTime();
        entity.setCreateTime(new Date(timestamp));
        messageMapper.insert(entity);
    }

    @Override
    public List<ChatMessage> findHistory(String roomId, int limit) {
        List<MessageEntity> entities = messageMapper.selectList(new LambdaQueryWrapper<MessageEntity>()
                .eq(MessageEntity::getRoomId, roomId)
                .orderByAsc(MessageEntity::getCreateTime)
                .last("limit " + limit));
        if (entities.isEmpty()) {
            return new ArrayList<>();
        }
        Set<String> senderIds = entities.stream()
                .map(MessageEntity::getSenderId)
                .filter(id -> null != id && !id.isEmpty())
                .collect(Collectors.toSet());
        Map<String, UserEntity> users = new HashMap<>();
        if (!senderIds.isEmpty()) {
            userMapper.selectBatchIds(senderIds).forEach(user -> users.put(user.getUserId(), user));
        }
        List<ChatMessage> messages = new ArrayList<>(entities.size());
        for (MessageEntity entity : entities) {
            ChatMessage message = new ChatMessage();
            message.setMessageId(entity.getMessageId());
            message.setRoomId(entity.getRoomId());
            message.setSenderId(entity.getSenderId());
            UserEntity user = users.get(entity.getSenderId());
            if (null != user) {
                message.setSenderName(user.getUserName());
                message.setSenderAvatar(user.getUserAvatar());
            }
            message.setMessageType(entity.getMessageType());
            message.setContent(entity.getMessageContent());
            message.setExt(entity.getMessageExt());
            message.setCreateTime(null == entity.getCreateTime() ? null : entity.getCreateTime().getTime());
            messages.add(message);
        }
        return messages;
    }
}
