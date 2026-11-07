import protobuf from 'protobufjs';

const root = await protobuf.load('public/proto/AIChatMsgProtocol.proto');
const commandEnum = root.lookupEnum('chat_msg.MsgCommand');
const toPascal = (name) => name.split('_').map((p) => p[0] + p.slice(1).toLowerCase()).join('');

const byCommand = new Map();
const commandByType = new Map();
for (const name of Object.keys(commandEnum.values)) {
  const id = commandEnum.values[name];
  const typeName = toPascal(name);
  let type;
  try {
    type = root.lookupType('chat_msg.' + typeName);
  } catch {
    continue;
  }
  byCommand.set(id, type);
  commandByType.set(type.name, id);
}

const ws = new WebSocket('ws://127.0.0.1:9090/');
ws.binaryType = 'arraybuffer';

let userId;
let roomId;
let gotBroadcast = false;
let gotAi = false;

const send = (typeName, payload) => {
  const command = commandByType.get(typeName);
  const type = byCommand.get(command);
  const body = type.encode(type.create(payload)).finish();
  const frame = new Uint8Array(6 + body.length);
  const view = new DataView(frame.buffer);
  view.setInt32(0, body.length, false);
  view.setInt16(4, command, false);
  frame.set(body, 6);
  ws.send(frame);
};

const parse = (data) => {
  const view = new DataView(data);
  const length = view.getInt32(0, false);
  const command = view.getInt16(4, false);
  const type = byCommand.get(command);
  if (!type) {
    return null;
  }
  const body = new Uint8Array(data, 6, Math.min(length, data.byteLength - 6));
  const obj = type.toObject(type.decode(body), { enums: String, longs: String, defaults: true, arrays: true });
  return { typeName: type.name, obj };
};

const waitFor = (cond, ms, what) => new Promise((resolve, reject) => {
  const start = Date.now();
  const timer = setInterval(() => {
    if (cond()) {
      clearInterval(timer);
      resolve();
    } else if (Date.now() - start > ms) {
      clearInterval(timer);
      reject(new Error('超时: ' + what));
    }
  }, 50);
});

ws.onopen = async () => {
  try {
    send('UserLoginCmd', { userName: 'WebTester', userAvatar: '' });
    await waitFor(() => userId, 5000, '登录');
    console.log('[OK] 登录', userId);

    send('CreateRoomCmd', { roomNo: 7201, roomName: '前端验证房间', history: 1, ai: 1 });
    await waitFor(() => roomId, 5000, '建房');
    console.log('[OK] 建房', roomId);

    send('SendMsgCmd', {
      msgId: 'web-' + Date.now(),
      roomId,
      msgType: 0,
      msg: '@AI 前端协议验证',
      ext: '',
    });
    await waitFor(() => gotBroadcast, 12000, '广播');
    console.log('[OK] 收到广播');
    await waitFor(() => gotAi, 15000, 'AI 回复');
    console.log('[OK] 收到 AI 回复');
    console.log('[DONE] protobuf.js 协议验证通过');
    ws.close();
    process.exit(0);
  } catch (e) {
    console.error('[FAIL]', e.message);
    process.exit(1);
  }
};

ws.onmessage = (event) => {
  const inbound = parse(event.data);
  if (!inbound) {
    return;
  }
  if (inbound.typeName === 'UserLoginAck') {
    userId = inbound.obj.userId;
  } else if (inbound.typeName === 'CreateRoomAck') {
    roomId = inbound.obj.roomId;
  } else if (inbound.typeName === 'BroadcastMsgAck') {
    gotBroadcast = true;
    console.log('[recv] BroadcastMsgAck:', inbound.obj.msg);
  } else if (inbound.typeName === 'AiReplyMsgAck') {
    gotAi = true;
    console.log('[recv] AiReplyMsgAck:', inbound.obj.msg);
  } else if (inbound.typeName === 'ExceptionMsg') {
    console.log('[recv] ExceptionMsg:', inbound.obj.code, inbound.obj.msg);
  }
};

ws.onerror = () => {
  console.error('[FAIL] WebSocket 连接失败');
  process.exit(1);
};

setTimeout(() => {
  console.error('[FAIL] 全局超时');
  process.exit(1);
}, 45000);
