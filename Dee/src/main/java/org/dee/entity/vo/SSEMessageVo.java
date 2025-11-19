package org.dee.entity.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SSEMessageVo {
    private String userId;
    private String conversationId;
    private String type;
    private String message;
    private LocalDateTime timestamp;

    public SSEMessageVo(String userId, String conversationId, String type, String message, LocalDateTime timestamp) {
        this.userId = userId;
        this.conversationId = conversationId;
        this.type = type;
        this.message = message;
        this.timestamp = timestamp;
    }
}
