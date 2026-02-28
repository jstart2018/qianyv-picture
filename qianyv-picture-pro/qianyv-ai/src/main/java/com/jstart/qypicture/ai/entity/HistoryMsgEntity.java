package com.jstart.qypicture.ai.entity;

import lombok.Data;

@Data
public class HistoryMsgEntity {

    /**
     * 角色
     */
    private String messageType;

    /**
     * 内容
     */
    private String textContent;

    @Override
    public String toString() {
        return "{" +
                "messageType='" + messageType + '\'' +
                ", textContent='" + textContent + '\'' +
                '}';
    }

}
