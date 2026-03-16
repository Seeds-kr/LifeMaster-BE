package com.example.LifeMaster_BE.Group.GroupChat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

public class GroupChatDto {

    @Builder(toBuilder = true)
    @Getter
    @Setter
    @Schema(name = "ChatMessage")
    public static class ChatMessage {
        private Long id;          // DB 저장 후 메시지 ID
        private Long groupId;
        private Long senderId;
        private String senderName;
        private String message;
        private LocalDateTime timestamp;
        public static ChatMessage from(GroupChatEntity entity) {
            return ChatMessage.builder()
                    .id(entity.getId())
                    .groupId(entity.getGroup().getId())
                    .senderId(entity.getSender().getId())
                    .senderName(entity.getSender().getNickname())
                    .message(entity.getMessage())
                    .timestamp(entity.getTimestamp())
                    .build();
        }
    }

    @Builder(toBuilder = true)
    @Getter
    @Schema(name = "ChatReadEvent")
    public static class ChatReadEvent {
        private Long groupId;
        private Long messageId;
        private Long readerId;
    }
}
