-- AI Chat 数据库初始化脚本（MySQL 8+）
CREATE DATABASE IF NOT EXISTS `ai_chat`
    DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE `ai_chat`;

-- 用户表
CREATE TABLE IF NOT EXISTS `aq_user` (
    `user_id`     varchar(128) NOT NULL COMMENT '用户id',
    `user_name`   varchar(255) NOT NULL DEFAULT '' COMMENT '用户昵称',
    `user_avatar` text COMMENT '用户头像',
    PRIMARY KEY (`user_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT ='用户';

-- 消息表
CREATE TABLE IF NOT EXISTS `aq_message` (
    `message_id`      varchar(128)  NOT NULL COMMENT '消息id',
    `room_id`         varchar(128)  NOT NULL DEFAULT '' COMMENT '房间id',
    `sender_id`       varchar(128)  NOT NULL DEFAULT '' COMMENT '发送者id',
    `message_type`    int           NOT NULL DEFAULT 0 COMMENT '消息类型 0文本 1图片 2语音 3视频 4文件',
    `message_content` longtext      NOT NULL COMMENT '消息内容',
    `message_ext`     varchar(1024) NOT NULL DEFAULT '' COMMENT '扩展字段',
    `status`          tinyint       NOT NULL DEFAULT 1 COMMENT '消息状态 0不显示 1显示',
    `create_time`     datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`message_id`),
    KEY `idx_room_create` (`room_id`, `create_time`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT ='聊天消息';

-- 内置 AI 助手用户（历史消息回显时用于补全发送者信息）
INSERT INTO `aq_user` (`user_id`, `user_name`, `user_avatar`)
VALUES ('aichat_ai', '小Q', '')
ON DUPLICATE KEY UPDATE `user_name` = VALUES(`user_name`);
