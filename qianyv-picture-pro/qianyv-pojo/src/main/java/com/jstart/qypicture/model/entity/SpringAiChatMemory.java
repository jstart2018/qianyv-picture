package com.jstart.qypicture.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;

import lombok.Builder;
import lombok.Data;

/**
 * 
 * @TableName spring_ai_chat_memory
 */
@Builder
@TableName(value ="spring_ai_chat_memory")
@Data
public class SpringAiChatMemory {
    /**
     * 
     */
    private String conversationId;

    /**
     * 
     */
    private String content;

    /**
     * 
     */
    private String type;

    /**
     * 
     */
    private Date timestamp;
}