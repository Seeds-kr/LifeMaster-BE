package com.example.LifeMaster_BE.Group.GroupChat;

import com.example.LifeMaster_BE.Group.GroupEntity;
import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class GroupChatService {

    private final GroupChatRepository chatMessageRepository;

    // 메시지 저장
    public GroupChatEntity saveMessage(GroupChatDto.ChatMessage dto, GroupEntity group, MemberEntity sender) {
        GroupChatEntity entity = GroupChatEntity.builder()
                .group(group)
                .sender(sender)
                .message(dto.getMessage())
                .timestamp(LocalDateTime.now())
                .build();

        chatMessageRepository.save(entity);

        dto.setId(entity.getId());
        dto.setGroupId(group.getId());
        dto.setSenderId(sender.getId());
        dto.setSenderName(sender.getNickname());
        dto.setTimestamp(entity.getTimestamp());

        return entity;
    }

    // 읽음 처리
    public void markAsRead(Long messageId, MemberEntity reader) {
        chatMessageRepository.findById(messageId).ifPresent(messageEntity -> {
            messageEntity.getReadBy().add(reader);
            chatMessageRepository.save(messageEntity);
        });
    }
}
