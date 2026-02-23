package com.jstart.qypicture.service.impl;


import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jstart.qypicture.mapper.SpringAiChatMemoryMapper;
import com.jstart.qypicture.model.dto.ChatMemoryQueryDTO;
import com.jstart.qypicture.model.entity.SpringAiChatMemory;
import com.jstart.qypicture.model.vo.ChatMemoryVO;
import com.jstart.qypicture.service.SpringAiChatMemoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
* @author 28435
* @description 针对表【spring_ai_chat_memory】的数据库操作Service实现
* @createDate 2026-02-23 14:12:37
*/
@Slf4j
@Service
public class SpringAiChatMemoryServiceImpl extends ServiceImpl<SpringAiChatMemoryMapper, SpringAiChatMemory>
    implements SpringAiChatMemoryService {

    @Override
    public List<ChatMemoryVO> listByCursor(ChatMemoryQueryDTO queryDTO) {
        // 构建查询条件
        LambdaQueryWrapper<SpringAiChatMemory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SpringAiChatMemory::getConversationId, queryDTO.getConversationId());
        
        // 如果有游标，查询游标之前的数据（时间更早的）
        if (queryDTO.getCursor() != null) {
            queryWrapper.lt(SpringAiChatMemory::getTimestamp, queryDTO.getCursor());
        }
        
        // 按时间倒序排列，最新的在前面
        queryWrapper.orderByDesc(SpringAiChatMemory::getTimestamp);
        
        // 限制查询数量
        Integer pageSize = queryDTO.getPageSize() != null ? queryDTO.getPageSize() : 20;
        queryWrapper.last("LIMIT " + pageSize);
        
        // 执行查询
        List<SpringAiChatMemory> memoryList = this.list(queryWrapper);
        
        // 转换为VO并反转顺序（让时间早的在前面，符合聊天记录展示顺序）
        List<ChatMemoryVO> voList = memoryList.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        
        // 反转列表，让时间早的在前面
        java.util.Collections.reverse(voList);
        
        return voList;
    }






    @Override
    public List<SpringAiChatMemory> listByConversationId(String conversationId) {
        LambdaQueryWrapper<SpringAiChatMemory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SpringAiChatMemory::getConversationId, conversationId);
        List<SpringAiChatMemory> messages = this.list(queryWrapper);
        return messages;
    }

    @Override
    public boolean deleteByConversationId(String conversationId) {
        return this.lambdaUpdate()
                .eq(SpringAiChatMemory::getConversationId, conversationId)
                .remove();
    }

    /**
     * 实体转换为VO，提取textContent字段
     * 支持两种格式：
     * 1. 新格式：JSON对象 {"textContent": "内容", ...}
     * 2. 旧格式：纯文本
     */
    private ChatMemoryVO convertToVO(SpringAiChatMemory memory) {
        ChatMemoryVO vo = new ChatMemoryVO();
        BeanUtils.copyProperties(memory, vo);
        
        // type字段是Object类型，需要转换为String
        if (memory.getType() != null) {
            vo.setType(memory.getType().toString());
        }
        
        // 提取content字段中的textContent
        String rawContent = memory.getContent();
        if (rawContent != null && rawContent.trim().startsWith("{")) {
            try {
                JSONObject json = JSONUtil.parseObj(rawContent);
                String textContent = json.getStr("textContent");
                if (textContent != null) {
                    vo.setContent(textContent);
                } else {
                    // 如果没有textContent字段，使用原始内容
                    vo.setContent(rawContent);
                }
            } catch (Exception e) {
                log.warn("解析JSON内容失败，使用原始内容: {}", e.getMessage());
                vo.setContent(rawContent);
            }
        } else {
            // 旧格式，直接使用原始内容
            vo.setContent(rawContent);
        }
        
        return vo;
    }
}