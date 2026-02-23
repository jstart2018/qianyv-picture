package com.jstart.qypicture.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * AI聊天记录VO
 */
@Data
public class ChatMemoryVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 会话ID
     */
    private String conversationId;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 消息类型（USER/ASSISTANT/SYSTEM）
     */
    private String type;

    /**
     * 消息时间戳
     */
    private Date timestamp;
}
