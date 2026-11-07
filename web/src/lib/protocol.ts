import * as protobuf from 'protobufjs';

export interface Inbound {
  typeName: string;
  payload: any;
}

export interface ChatProtocol {
  send(typeName: string, payload: Record<string, unknown>): void;
  parse(data: ArrayBuffer): Inbound | null;
}

/** 枚举名转消息名：USER_LOGIN_CMD -> UserLoginCmd */
function toPascal(name: string): string {
  return name
    .split('_')
    .map((part) => part.charAt(0) + part.slice(1).toLowerCase())
    .join('');
}

/**
 * 加载 .proto，建立「命令号 <-> 消息类型」映射，并实现与服务端一致的帧编解码：
 * 4 字节长度 + 2 字节命令号 + Protobuf 消息体。
 */
export async function createProtocol(ws: WebSocket): Promise<ChatProtocol> {
  const root = await protobuf.load('/proto/AIChatMsgProtocol.proto');
  const commandEnum = root.lookupEnum('chat_msg.MsgCommand');

  const byCommand = new Map<number, protobuf.Type>();
  const commandByType = new Map<string, number>();

  for (const name of Object.keys(commandEnum.values)) {
    const command = commandEnum.values[name];
    const typeName = toPascal(name);
    let type: protobuf.Type;
    try {
      type = root.lookupType('chat_msg.' + typeName);
    } catch {
      continue;
    }
    byCommand.set(command, type);
    commandByType.set(type.name, command);
  }

  return {
    send(typeName, payload) {
      const command = commandByType.get(typeName);
      if (command === undefined) {
        throw new Error('未知消息类型: ' + typeName);
      }
      const type = byCommand.get(command)!;
      const error = type.verify(payload);
      if (error) {
        throw new Error(error);
      }
      const body = type.encode(type.create(payload)).finish();
      const frame = new Uint8Array(6 + body.length);
      const view = new DataView(frame.buffer);
      view.setInt32(0, body.length, false);
      view.setInt16(4, command, false);
      frame.set(body, 6);
      ws.send(frame);
    },

    parse(data) {
      if (data.byteLength < 6) {
        return null;
      }
      const view = new DataView(data);
      const length = view.getInt32(0, false);
      const command = view.getInt16(4, false);
      const type = byCommand.get(command);
      if (!type) {
        return null;
      }
      const available = data.byteLength - 6;
      const body = new Uint8Array(data, 6, Math.min(length, available));
      const message = type.decode(body);
      const payload = type.toObject(message, {
        enums: String,
        longs: String,
        defaults: true,
        arrays: true,
      });
      return { typeName: type.name, payload };
    },
  };
}
