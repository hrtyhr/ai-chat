package com.howcode.aichat.transport.dispatch;

import com.howcode.aichat.core.domain.error.ErrorCode;
import com.howcode.aichat.core.domain.model.ChatMessage;
import com.howcode.aichat.core.domain.model.RecallInfo;
import com.howcode.aichat.core.domain.model.RoomInfo;
import com.howcode.aichat.core.domain.model.UserInfo;
import com.howcode.aichat.transport.protocol.AIChatMsgProtocol;

import java.util.List;

/** 领域对象 -> Protobuf 消息。集中在接入层，core 不感知协议。 */
public final class ResponseFactory {

    private ResponseFactory() {
    }

    public static AIChatMsgProtocol.ExceptionMsg exception(ErrorCode code) {
        return AIChatMsgProtocol.ExceptionMsg.newBuilder()
                .setCode(code.code())
                .setMsg(code.message())
                .build();
    }

    public static AIChatMsgProtocol.UserLoginAck loginAck(UserInfo user) {
        return AIChatMsgProtocol.UserLoginAck.newBuilder()
                .setUserId(nullSafe(user.getUserId()))
                .setUserName(nullSafe(user.getUserName()))
                .setUserAvatar(nullSafe(user.getUserAvatar()))
                .build();
    }

    public static AIChatMsgProtocol.UserLogoutAck logoutAck(String userId) {
        return AIChatMsgProtocol.UserLogoutAck.newBuilder().setUserId(nullSafe(userId)).build();
    }

    public static AIChatMsgProtocol.HeartBeatAck heartbeatAck() {
        return AIChatMsgProtocol.HeartBeatAck.newBuilder().setPong("AIChat-Ack").build();
    }

    public static AIChatMsgProtocol.CreateRoomAck createRoomAck(RoomInfo room) {
        return AIChatMsgProtocol.CreateRoomAck.newBuilder()
                .setRoomId(nullSafe(room.getRoomId()))
                .setRoomNo(nullSafe(room.getRoomNo()))
                .setRoomName(nullSafe(room.getRoomName()))
                .setAi(nullSafe(room.getAi()))
                .build();
    }

    public static AIChatMsgProtocol.JoinRoomAck joinRoomAck(RoomInfo room) {
        return AIChatMsgProtocol.JoinRoomAck.newBuilder()
                .setRoomId(nullSafe(room.getRoomId()))
                .setRoomNo(nullSafe(room.getRoomNo()))
                .setRoomName(nullSafe(room.getRoomName()))
                .setAi(nullSafe(room.getAi()))
                .build();
    }

    public static AIChatMsgProtocol.LeaveRoomAck leaveRoomAck(String roomId) {
        return AIChatMsgProtocol.LeaveRoomAck.newBuilder().setRoomId(nullSafe(roomId)).build();
    }

    public static AIChatMsgProtocol.SendMsgAck sendAck(String roomId, String userId, String msgId, String ext) {
        return AIChatMsgProtocol.SendMsgAck.newBuilder()
                .setRoomId(nullSafe(roomId))
                .setUserId(nullSafe(userId))
                .setMsgId(nullSafe(msgId))
                .setExt(nullSafe(ext))
                .setStatus(true)
                .build();
    }

    public static AIChatMsgProtocol.SyncChatRecordAck historyAck(String roomId, List<ChatMessage> messages) {
        AIChatMsgProtocol.SyncChatRecordAck.Builder builder = AIChatMsgProtocol.SyncChatRecordAck.newBuilder()
                .setRoomId(nullSafe(roomId));
        if (null != messages) {
            for (ChatMessage message : messages) {
                builder.addChatRecords(AIChatMsgProtocol.ChatRecord.newBuilder()
                        .setMsgId(nullSafe(message.getMessageId()))
                        .setUser(userOf(message))
                        .setMsgType(msgTypeOf(message.getMessageType()))
                        .setMessage(nullSafe(message.getContent()))
                        .setExt(nullSafe(message.getExt()))
                        .setCreateTime(message.getCreateTime() == null ? "" : String.valueOf(message.getCreateTime()))
                        .build());
            }
        }
        return builder.build();
    }

    public static AIChatMsgProtocol.SyncRoomMembersAck membersAck(String roomId, List<UserInfo> members) {
        AIChatMsgProtocol.SyncRoomMembersAck.Builder builder = AIChatMsgProtocol.SyncRoomMembersAck.newBuilder()
                .setRoomId(nullSafe(roomId));
        if (null != members) {
            members.forEach(member -> builder.addMembers(userOf(member)));
        }
        return builder.build();
    }

    public static AIChatMsgProtocol.RecallMsgAck recallAck(String roomId, String msgId, String userId) {
        return AIChatMsgProtocol.RecallMsgAck.newBuilder()
                .setRoomId(nullSafe(roomId))
                .setMsgId(nullSafe(msgId))
                .setUserId(nullSafe(userId))
                .setStatus(true)
                .build();
    }

    public static AIChatMsgProtocol.BroadcastMsgAck broadcastChat(ChatMessage message) {
        return AIChatMsgProtocol.BroadcastMsgAck.newBuilder()
                .setRoomId(nullSafe(message.getRoomId()))
                .setMsgId(nullSafe(message.getMessageId()))
                .setUser(userOf(message))
                .setMsgType(msgTypeOf(message.getMessageType()))
                .setMsg(nullSafe(message.getContent()))
                .setExt(nullSafe(message.getExt()))
                .build();
    }

    public static AIChatMsgProtocol.JoinRoomNotify joinNotice(String roomId, UserInfo user) {
        return AIChatMsgProtocol.JoinRoomNotify.newBuilder()
                .setRoomId(nullSafe(roomId))
                .setUser(userOf(user))
                .build();
    }

    public static AIChatMsgProtocol.LeaveRoomNotify leaveNotice(String roomId, UserInfo user) {
        return AIChatMsgProtocol.LeaveRoomNotify.newBuilder()
                .setRoomId(nullSafe(roomId))
                .setUser(userOf(user))
                .build();
    }

    public static AIChatMsgProtocol.OfflineNotify offlineNotice(String roomId, UserInfo user) {
        return AIChatMsgProtocol.OfflineNotify.newBuilder()
                .setRoomId(nullSafe(roomId))
                .setUser(userOf(user))
                .build();
    }

    public static AIChatMsgProtocol.RecallMsgNotify recallNotify(RecallInfo info) {
        return AIChatMsgProtocol.RecallMsgNotify.newBuilder()
                .setRoomId(nullSafe(info.roomId()))
                .setMsgId(nullSafe(info.messageId()))
                .setUserId(nullSafe(info.userId()))
                .build();
    }

    public static AIChatMsgProtocol.AiReplyMsgAck aiReply(ChatMessage message, int status) {
        return AIChatMsgProtocol.AiReplyMsgAck.newBuilder()
                .setRoomId(nullSafe(message.getRoomId()))
                .setMsgId(nullSafe(message.getMessageId()))
                .setUser(userOf(message))
                .setMsg(nullSafe(message.getContent()))
                .setExt(nullSafe(message.getExt()))
                .setMsgType(msgTypeOf(message.getMessageType()))
                .setStatus(status)
                .build();
    }

    public static AIChatMsgProtocol.OfflineMsg offlineMsg(UserInfo user, String roomId) {
        return AIChatMsgProtocol.OfflineMsg.newBuilder()
                .setUser(userOf(user))
                .setRoomId(nullSafe(roomId))
                .build();
    }

    private static AIChatMsgProtocol.User userOf(UserInfo user) {
        if (null == user) {
            return AIChatMsgProtocol.User.getDefaultInstance();
        }
        return AIChatMsgProtocol.User.newBuilder()
                .setUserId(nullSafe(user.getUserId()))
                .setUserName(nullSafe(user.getUserName()))
                .setUserAvatar(nullSafe(user.getUserAvatar()))
                .build();
    }

    private static AIChatMsgProtocol.User userOf(ChatMessage message) {
        return AIChatMsgProtocol.User.newBuilder()
                .setUserId(nullSafe(message.getSenderId()))
                .setUserName(nullSafe(message.getSenderName()))
                .setUserAvatar(nullSafe(message.getSenderAvatar()))
                .build();
    }

    private static AIChatMsgProtocol.MsgType msgTypeOf(Integer type) {
        if (null == type) {
            return AIChatMsgProtocol.MsgType.TEXT;
        }
        AIChatMsgProtocol.MsgType msgType = AIChatMsgProtocol.MsgType.forNumber(type);
        return null == msgType ? AIChatMsgProtocol.MsgType.TEXT : msgType;
    }

    private static String nullSafe(String value) {
        return null == value ? "" : value;
    }

    private static Integer nullSafe(Integer value) {
        return null == value ? 0 : value;
    }
}
