package com.jstart.qypicture.service;

import com.jstart.qypicture.model.dto.ChatMemoryQueryDTO;
import com.jstart.qypicture.model.entity.SpringAiChatMemory;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jstart.qypicture.model.vo.ChatMemoryVO;

import java.util.List;

/**
* @author 28435
* @description 针对表【spring_ai_chat_memory】的数据库操作Service
* @createDate 2026-02-23 14:12:37
*/
public interface SpringAiChatMemoryService extends IService<SpringAiChatMemory> {

    /**
     * 游标查询聊天记录
     * @param queryDTO 查询参数
     * @return 聊天记录列表
     */
    List<ChatMemoryVO> listByCursor(ChatMemoryQueryDTO queryDTO);

    /**
     * 根据会话ID查询聊天记录
     * @param conversationId 会话ID
     * @return 聊天记录列表
     */
    List<SpringAiChatMemory> listByConversationId(String conversationId);

    boolean deleteByConversationId(String conversationId);
}
