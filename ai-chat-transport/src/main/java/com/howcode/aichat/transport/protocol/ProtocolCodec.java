package com.howcode.aichat.transport.protocol;

import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.Message;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * 二进制协议编解码：4 字节长度 + 2 字节命令号 + Protobuf 消息体。
 */
@Component
public class ProtocolCodec {

    @Resource
    private ProtocolRecognizer recognizer;

    public GeneratedMessageV3 decode(byte[] frame) throws Exception {
        if (null == frame || frame.length < 6) {
            return null;
        }
        ByteBuffer buffer = ByteBuffer.wrap(frame).order(ByteOrder.BIG_ENDIAN);
        int length = buffer.getInt();
        short command = buffer.getShort();
        if (length < 0 || length > buffer.remaining()) {
            return null;
        }
        byte[] body = new byte[length];
        buffer.get(body);
        Message.Builder builder = recognizer.builderOf(command);
        if (null == builder) {
            return null;
        }
        return (GeneratedMessageV3) builder.mergeFrom(body).build();
    }

    public byte[] encode(GeneratedMessageV3 message) {
        int command = recognizer.commandOf(message.getClass());
        if (command < 0) {
            return null;
        }
        byte[] body = message.toByteArray();
        ByteBuffer buffer = ByteBuffer.allocate(4 + 2 + body.length).order(ByteOrder.BIG_ENDIAN);
        buffer.putInt(body.length);
        buffer.putShort((short) command);
        buffer.put(body);
        return buffer.array();
    }

    public void send(Channel channel, GeneratedMessageV3 message) {
        byte[] frame = encode(message);
        if (null == frame || !channel.isActive()) {
            return;
        }
        channel.writeAndFlush(new BinaryWebSocketFrame(Unpooled.wrappedBuffer(frame)));
    }
}
