package com.howcode.aichat.core.domain.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 房间领域模型。
 */
@Data
public class RoomInfo {
    private String roomId;
    private Integer roomNo;
    private String roomName;
    /** 房间类型，见 RoomKind */
    private Integer roomType;
    /** 是否保留历史消息 0/1 */
    private Integer history;
    /** 是否开启 AI 0/1 */
    private Integer ai;
    private List<UserInfo> members = new ArrayList<>();
}
