import { useCallback, useEffect, useRef, useState } from 'react';
import { createProtocol, ChatProtocol } from './lib/protocol';

type Phase = 'connecting' | 'login' | 'lobby' | 'room' | 'offline';

interface UserState {
  userId: string;
  userName: string;
  userAvatar: string;
}

interface RoomState {
  roomId: string;
  roomNo: number;
  roomName: string;
  ai: number;
}

interface ChatItem {
  id: string;
  kind: 'chat' | 'system';
  userId?: string;
  name: string;
  avatar?: string;
  text: string;
  time?: string;
  self?: boolean;
  ai?: boolean;
  recalled?: boolean;
}

const WS_URL = (import.meta as any).env?.VITE_WS_URL || 'ws://127.0.0.1:9090/';
const AI_USER_ID = 'aichat_ai';

function nowTime(): string {
  return new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' });
}

function formatTime(value?: string): string {
  if (!value) {
    return '';
  }
  const numeric = Number(value);
  const date = Number.isFinite(numeric) ? new Date(numeric) : new Date(value);
  return isNaN(date.getTime()) ? '' : date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' });
}

function newMsgId(): string {
  if (typeof crypto !== 'undefined' && 'randomUUID' in crypto) {
    return crypto.randomUUID();
  }
  return 'm-' + Date.now() + '-' + Math.random().toString(16).slice(2);
}

export default function App() {
  const [phase, setPhase] = useState<Phase>('connecting');
  const [status, setStatus] = useState('连接中...');
  const [user, setUser] = useState<UserState | null>(null);
  const [room, setRoom] = useState<RoomState | null>(null);
  const [messages, setMessages] = useState<ChatItem[]>([]);
  const [members, setMembers] = useState<{ userId: string; userName: string; userAvatar: string }[]>([]);
  const [toast, setToast] = useState('');

  const [loginName, setLoginName] = useState('');
  const [loginAvatar, setLoginAvatar] = useState('');
  const [createNo, setCreateNo] = useState('7101');
  const [createName, setCreateName] = useState('我的房间');
  const [createAi, setCreateAi] = useState(true);
  const [joinNo, setJoinNo] = useState('7101');
  const [input, setInput] = useState('');

  const wsRef = useRef<WebSocket | null>(null);
  const protocolRef = useRef<ChatProtocol | null>(null);
  const heartbeatRef = useRef<number | null>(null);
  const userRef = useRef<UserState | null>(null);
  const roomRef = useRef<RoomState | null>(null);
  const handleInboundRef = useRef<(type: string, payload: any) => void>(() => undefined);
  const bottomRef = useRef<HTMLDivElement | null>(null);

  useEffect(() => {
    userRef.current = user;
  }, [user]);

  useEffect(() => {
    roomRef.current = room;
  }, [room]);

  useEffect(() => {
    if (!toast) {
      return;
    }
    const timer = window.setTimeout(() => setToast(''), 3200);
    return () => window.clearTimeout(timer);
  }, [toast]);

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  const send = useCallback((type: string, payload: Record<string, unknown>) => {
    try {
      protocolRef.current?.send(type, payload);
    } catch (e: any) {
      setToast('发送失败: ' + (e?.message ?? e));
    }
  }, []);

  const pushSystem = useCallback((text: string) => {
    setMessages((prev) => [...prev.slice(-499), {
      id: newMsgId(), kind: 'system', name: '系统', text, time: nowTime(),
    }]);
  }, []);

  const pushChat = useCallback((item: ChatItem) => {
    setMessages((prev) => [...prev.slice(-499), item]);
  }, []);

  const handleInbound = useCallback((type: string, payload: any) => {
    switch (type) {
      case 'UserLoginAck':
        setUser({ userId: payload.userId, userName: payload.userName, userAvatar: payload.userAvatar });
        setPhase('lobby');
        break;
      case 'CreateRoomAck':
      case 'JoinRoomAck':
        setRoom({
          roomId: payload.roomId,
          roomNo: payload.roomNo,
          roomName: payload.roomName,
          ai: payload.ai,
        });
        setMessages([]);
        setMembers([]);
        setPhase('room');
        send('SyncRoomMembersCmd', { roomId: payload.roomId });
        send('SyncChatRecordCmd', { roomId: payload.roomId });
        break;
      case 'LeaveRoomAck':
        setRoom(null);
        setMessages([]);
        setMembers([]);
        setPhase('lobby');
        break;
      case 'UserLogoutAck':
        setUser(null);
        setRoom(null);
        setMessages([]);
        setMembers([]);
        setPhase('login');
        break;
      case 'BroadcastMsgAck': {
        const userId = payload.user?.userId;
        pushChat({
          id: payload.msgId,
          kind: 'chat',
          userId,
          name: payload.user?.userName || '匿名',
          avatar: payload.user?.userAvatar,
          text: payload.msg || '',
          time: nowTime(),
          self: userId === userRef.current?.userId,
          ai: userId === AI_USER_ID,
        });
        break;
      }
      case 'AiReplyMsgAck':
        pushChat({
          id: payload.msgId,
          kind: 'chat',
          userId: AI_USER_ID,
          name: payload.user?.userName || '小Q',
          avatar: payload.user?.userAvatar,
          text: payload.msg || '',
          time: nowTime(),
          ai: true,
        });
        break;
      case 'SyncChatRecordAck': {
        const records = payload.chatRecords || [];
        setMessages(records.map((record: any) => ({
          id: record.msgId,
          kind: 'chat',
          userId: record.user?.userId,
          name: record.user?.userName || '匿名',
          avatar: record.user?.userAvatar,
          text: record.message || '',
          time: formatTime(record.createTime),
          self: record.user?.userId === userRef.current?.userId,
          ai: record.user?.userId === AI_USER_ID,
        })));
        break;
      }
      case 'SyncRoomMembersAck':
        setMembers(payload.members || []);
        break;
      case 'JoinRoomNotify':
        pushSystem(`${payload.user?.userName || '有人'} 加入了房间`);
        if (roomRef.current) send('SyncRoomMembersCmd', { roomId: roomRef.current.roomId });
        break;
      case 'LeaveRoomNotify':
        pushSystem(`${payload.user?.userName || '有人'} 离开了房间`);
        if (roomRef.current) send('SyncRoomMembersCmd', { roomId: roomRef.current.roomId });
        break;
      case 'OfflineNotify':
        pushSystem(`${payload.user?.userName || '有人'} 掉线了`);
        break;
      case 'RecallMsgNotify':
        setMessages((prev) => prev.map((item) =>
          item.id === payload.msgId ? { ...item, recalled: true, text: '该消息已撤回' } : item));
        break;
      case 'ExceptionMsg':
        setToast(`[${payload.code}] ${payload.msg}`);
        break;
      case 'OfflineMsg':
        setToast('长时间未活动，已被服务端断开');
        break;
      default:
        break;
    }
  }, [pushChat, pushSystem, send]);

  handleInboundRef.current = handleInbound;

  useEffect(() => {
    const ws = new WebSocket(WS_URL);
    ws.binaryType = 'arraybuffer';
    wsRef.current = ws;

    ws.onopen = async () => {
      try {
        protocolRef.current = await createProtocol(ws);
        setStatus('已连接');
        setPhase('login');
        heartbeatRef.current = window.setInterval(() => {
          try {
            protocolRef.current?.send('HeartBeatCmd', { ping: 'ping' });
          } catch {
            // 连接已关闭时忽略
          }
        }, 25000);
      } catch (e: any) {
        setStatus('协议加载失败: ' + (e?.message ?? e));
      }
    };

    ws.onmessage = (event) => {
      if (!protocolRef.current) {
        return;
      }
      const inbound = protocolRef.current.parse(event.data as ArrayBuffer);
      if (inbound) {
        handleInboundRef.current(inbound.typeName, inbound.payload);
      }
    };

    ws.onclose = () => {
      setStatus('连接已断开');
      setPhase('offline');
      if (heartbeatRef.current) {
        window.clearInterval(heartbeatRef.current);
      }
    };

    ws.onerror = () => setStatus('连接错误');

    return () => {
      if (heartbeatRef.current) {
        window.clearInterval(heartbeatRef.current);
      }
      ws.close();
    };
  }, []);

  const doLogin = () => {
    if (!loginName.trim()) {
      setToast('请输入昵称');
      return;
    }
    send('UserLoginCmd', { userName: loginName.trim(), userAvatar: loginAvatar.trim() });
  };

  const parseRoomNo = (value: string): number | null => {
    const number = Number(value);
    return Number.isInteger(number) && number > 0 ? number : null;
  };

  const doCreate = () => {
    const roomNo = parseRoomNo(createNo);
    if (roomNo === null) {
      setToast('房间号需为正整数');
      return;
    }
    send('CreateRoomCmd', {
      roomNo,
      roomName: createName.trim() || '未命名房间',
      history: 1,
      ai: createAi ? 1 : 0,
    });
  };

  const doJoin = () => {
    const roomNo = parseRoomNo(joinNo);
    if (roomNo === null) {
      setToast('房间号需为正整数');
      return;
    }
    send('JoinRoomCmd', { roomNo });
  };

  const doSend = () => {
    const text = input.trim();
    if (!text || !roomRef.current) {
      return;
    }
    send('SendMsgCmd', {
      msgId: newMsgId(),
      roomId: roomRef.current.roomId,
      msgType: 0,
      msg: text,
      ext: '',
    });
    setInput('');
  };

  const onComposerKey = (event: React.KeyboardEvent<HTMLTextAreaElement>) => {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      doSend();
    }
  };

  const doLeave = () => {
    if (roomRef.current) {
      send('LeaveRoomCmd', { roomId: roomRef.current.roomId });
    }
  };

  const doLogout = () => send('UserLogoutCmd', { userId: userRef.current?.userId || '' });

  const doRecall = (msgId: string) => {
    if (roomRef.current) {
      send('RecallMsgCmd', { roomId: roomRef.current.roomId, msgId });
    }
  };

  return (
    <div className="app">
      <div className="topbar">
        <span className="brand">AI Chat</span>
        <span className={`status ${phase}`}>{status}</span>
      </div>

      {phase === 'connecting' && (
        <div className="center-card">
          <h2>正在连接服务器...</h2>
          <p className="muted">{WS_URL}</p>
        </div>
      )}

      {phase === 'offline' && (
        <div className="center-card">
          <h2>连接已断开</h2>
          <p className="muted">请确认后端已启动（Netty 9090），然后重试。</p>
          <button onClick={() => window.location.reload()}>重新连接</button>
        </div>
      )}

      {phase === 'login' && (
        <div className="center-card">
          <h2>加入 AI Chat</h2>
          <p className="muted">无需注册，输入昵称即可开始。</p>
          <input
            placeholder="昵称"
            value={loginName}
            maxLength={20}
            onChange={(e) => setLoginName(e.target.value)}
            onKeyDown={(e) => e.key === 'Enter' && doLogin()}
          />
          <input
            placeholder="头像 URL（可选）"
            value={loginAvatar}
            onChange={(e) => setLoginAvatar(e.target.value)}
          />
          <button onClick={doLogin}>进入</button>
        </div>
      )}

      {phase === 'lobby' && (
        <div className="lobby">
          <div className="card">
            <h2>创建房间</h2>
            <label>房间号<input value={createNo} onChange={(e) => setCreateNo(e.target.value)} /></label>
            <label>房间名<input value={createName} onChange={(e) => setCreateName(e.target.value)} /></label>
            <label className="check">
              <input type="checkbox" checked={createAi} onChange={(e) => setCreateAi(e.target.checked)} />
              开启 AI 助手（房间内所有文本都会触发 AI）
            </label>
            <button onClick={doCreate}>创建并进入</button>
          </div>

          <div className="card">
            <h2>加入房间</h2>
            <label>房间号<input value={joinNo} onChange={(e) => setJoinNo(e.target.value)} /></label>
            <button onClick={doJoin}>加入</button>
          </div>

          <div className="whoami">
            当前用户：<strong>{user?.userName}</strong>（{user?.userId}）
            <button className="link" onClick={doLogout}>退出登录</button>
          </div>
        </div>
      )}

      {phase === 'room' && room && (
        <div className="chat">
          <aside className="members">
            <h3>成员（{members.length}）</h3>
            <ul>
              {members.map((member) => (
                <li key={member.userId}>
                  <span className="dot" />
                  {member.userName}
                  {member.userId === AI_USER_ID ? ' 🤖' : ''}
                </li>
              ))}
            </ul>
            <div className="room-meta">
              <div>房间：{room.roomName}</div>
              <div>房号：#{room.roomNo}</div>
              <div>AI：{room.ai ? '已开启' : '未开启'}</div>
            </div>
          </aside>

          <main className="main">
            <header className="chat-header">
              <div>
                <strong>{room.roomName}</strong>
                <span className="muted"> #{room.roomNo}</span>
                {room.ai ? <span className="badge">AI</span> : null}
              </div>
              <button className="ghost" onClick={doLeave}>离开房间</button>
            </header>

            <div className="messages">
              {messages.map((item) => item.kind === 'system' ? (
                <div key={item.id} className="system">{item.text}</div>
              ) : (
                <div key={item.id} className={`msg ${item.self ? 'self' : ''} ${item.ai ? 'ai' : ''}`}>
                  <div className="avatar">
                    {item.avatar ? <img src={item.avatar} alt="" /> : (item.name?.[0] || '?')}
                  </div>
                  <div className="bubble-wrap">
                    <div className="meta">
                      <span>{item.name}{item.ai ? ' · AI' : ''}</span>
                      <span>{item.time}</span>
                    </div>
                    <div className={`bubble ${item.recalled ? 'recalled' : ''}`}>{item.text}</div>
                    {item.self && !item.recalled && !item.ai && (
                      <button className="link" onClick={() => doRecall(item.id)}>撤回</button>
                    )}
                  </div>
                </div>
              ))}
              <div ref={bottomRef} />
            </div>

            <div className="composer">
              <textarea
                value={input}
                placeholder="输入消息，Enter 发送，Shift+Enter 换行。消息含 @AI 可触发 AI 回复"
                onChange={(e) => setInput(e.target.value)}
                onKeyDown={onComposerKey}
              />
              <button onClick={doSend}>发送</button>
            </div>
          </main>
        </div>
      )}

      {toast && <div className="toast">{toast}</div>}
    </div>
  );
}
