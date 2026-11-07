# AI Chat

无需注册的匿名聊天，房间里自带 AI 助手。

基于 Netty 长连接的匿名即时通讯平台：创建 / 加入房间、实时聊天、历史记录、成员同步、消息撤回，以及房间级 AI 助手（`@AI` 触发、异步回复、Mock / 真实模型可切换）。

## 技术栈

- Spring Boot 3.2.5 / Java 17
- Netty 4.1（WebSocket 长连接）
- Redis（在线会话、房间、消息幂等）+ MySQL 8（用户、聊天记录）
- MyBatis-Plus 3.5.5 + RocketMQ 4.9（广播 / 落库，可关闭）
- OpenAI 兼容接口
- React 18 + TypeScript + Vite 5

## 环境要求

- JDK 17 或更高版本
- Maven 3.8+
- Docker Desktop（或本地 MySQL 8+、Redis 6+）
- RocketMQ 4.9、OpenAI 兼容 API Key
- Node.js 18+（前端）

## 启动

1. 启动 MySQL 与 Redis：

```bash
docker run -d --name ai-chat-mysql -p 3306:3306 -e MYSQL_ROOT_PASSWORD=123456 mysql:8
docker run -d --name ai-chat-redis -p 6379:6379 redis:7-alpine
```

容器就绪后，MySQL 暴露在 localhost:3306，Redis 暴露在 localhost:6379。默认数据库账号为 `root / 123456`，可通过环境变量 `MYSQL_USERNAME`、`MYSQL_PASSWORD` 覆盖。

2. 初始化数据库：

```bash
mysql -h127.0.0.1 -uroot -p123456 < sql/ai_chat.sql
```

3. 启动后端：

```bash
mvn clean package -DskipTests
java -jar ai-chat-bootstrap/target/ai-chat-bootstrap-1.0.0.jar
```

4. 启动前端：

```bash
cd web
npm install
npm run dev
```

访问 http://localhost:5173，输入昵称即可匿名登录。

后端默认监听 9090 端口。前端通过 `VITE_WS_URL` 指定其他地址。

## 配置项

配置文件：`ai-chat-bootstrap/src/main/resources/application.yml`

| 配置项 | 默认值 | 说明 |
|---|---|---|
| `aichat.netty.port` | 9090 | WebSocket 端口 |
| `aichat.netty.idle-minutes` | 10 | 空闲超时（分钟） |
| `aichat.ai.api-key` | 未配置 | 为空使用本地 Mock |
| `aichat.ai.base-url` | `https://api.openai.com/v1` | OpenAI 兼容地址 |
| `aichat.ai.model` | `gpt-4o-mini` | 对话模型 |
| `aichat.ai.timeout-seconds` | 30 | 请求超时 |
| `aichat.rocketmq.enabled` | true | 关闭后本地广播 / 落库 |
| `aichat.rocketmq.name-server` | `127.0.0.1:9876` | NameServer 地址 |
| `spring.datasource.*` | — | MySQL 连接 |
| `spring.data.redis.*` | — | Redis 连接 |
| `MYSQL_USERNAME` | root | 数据库用户（环境变量） |
| `MYSQL_PASSWORD` | 123456 | 数据库密码（环境变量） |

## AI 工作方式

- 房间创建时开启 AI，或消息内容包含 `@AI`（忽略大小写），触发 AI 回复。
- 仅文本消息触发；调用在独立线程池中异步执行，不阻塞 Netty IO 线程。
- 兼容 OpenAI 协议的模型服务，修改 `base-url` 与 `model` 即可接入。
- 未配置 `api-key` 时使用 Mock 网关，返回模拟回复，便于离线联调。
- 调用失败时返回友好提示，AI 消息标记为失败状态。
- AI 回复通过 WebSocket 广播给房间成员，并写入 MySQL。

## 构建

```bash
mvn clean package -DskipTests
java -jar ai-chat-bootstrap/target/ai-chat-bootstrap-1.0.0.jar
```

前端生产构建：

```bash
cd web
npm run build
```

## 重置本地数据

```bash
docker rm -f -v ai-chat-mysql ai-chat-redis
```

然后重新执行「启动」步骤。该命令会删除容器及其数据卷，仅在确认无需保留数据时使用。


