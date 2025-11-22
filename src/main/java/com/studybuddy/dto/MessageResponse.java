package com.studybuddy.dto;

import lombok.Data;

@Data
public class MessageResponse {
    private String id;
    private String sessionId;
    private String senderId;
    private String senderName;
    private String content;
    private Long timestamp;
}
