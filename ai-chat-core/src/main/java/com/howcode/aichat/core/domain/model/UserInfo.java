package com.howcode.aichat.core.domain.model;

import lombok.Data;

/**
 * 用户领域模型：既承载持久化信息，也承载当前会话所在房间。
 */
@Data
public class UserInfo {
    private String userId;
    private String userName;
    private String userAvatar;
    /** 当前所在房间，null 表示未进房 */
    private String roomId;
    /** 进房时间（epoch millis） */
    private Long joinRoomTime;
}
