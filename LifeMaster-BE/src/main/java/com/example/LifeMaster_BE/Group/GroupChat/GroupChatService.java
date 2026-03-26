package com.example.LifeMaster_BE.Group.GroupChat;

import com.example.LifeMaster_BE.Group.GroupEntity;
import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import com.example.LifeMaster_BE.UserManager.Member.MemberRepository;
import com.example.LifeMaster_BE.UserManager.Member.Subscription.FeatureType;
import com.example.LifeMaster_BE.UserManager.Member.Subscription.SubscriptionAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupChatService {

    private final GroupChatRepository chatMessageRepository;
    private final MemberRepository memberRepository;
    private final SubscriptionAccessService subscriptionAccessService;


    // 메시지 저장
    public GroupChatEntity saveMessage(GroupChatDto.ChatMessage dto, GroupEntity group, MemberEntity sender) {

        MemberEntity member = getMemberOrThrow(sender.getId());
        // 프리미엄 기능 접근 검사
        subscriptionAccessService.validateFeatureAccess(member, FeatureType.GROUP);

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

        MemberEntity member = getMemberOrThrow(reader.getId());
        // 프리미엄 기능 접근 검사
        subscriptionAccessService.validateFeatureAccess(member, FeatureType.GROUP);

        chatMessageRepository.findById(messageId).ifPresent(messageEntity -> {
            messageEntity.getReadBy().add(reader);
            chatMessageRepository.save(messageEntity);
        });
    }

    public List<GroupChatDto.ChatMessage> getChatsByGroup(Long groupId) {
        return chatMessageRepository
                .findByGroupIdOrderByTimestampAsc(groupId)
                .stream()
                .map(GroupChatDto.ChatMessage::from)
                .collect(Collectors.toList());
    }

    private MemberEntity getMemberOrThrow(Long userId) {
        return memberRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
    }
}
