package com.howcode.aichat.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("aq_message")
public class MessageEntity {
    @TableId("message_id")
    private String messageId;
    private String roomId;
    private String senderId;
    private Integer messageType;
    private String messageContent;
    private String messageExt;
    private Integer status;
    private Date createTime;
}
