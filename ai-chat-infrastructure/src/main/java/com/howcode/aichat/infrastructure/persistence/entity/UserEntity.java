package com.howcode.aichat.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("aq_user")
public class UserEntity {
    @TableId("user_id")
    private String userId;
    private String userName;
    private String userAvatar;
}
