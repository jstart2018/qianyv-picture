package com.jstart.qypicture.model.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * AI聊天记录游标查询请求DTO
 */
@Data
public class ChatMemoryQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 会话ID
     */
    private String conversationId;

    /**
     * 游标（上次查询最后一条记录的时间戳，首次查询不传）
     */
    private Date cursor;

    /**
     * 每页大小，默认20
     */
    private Integer pageSize = 10;
}
