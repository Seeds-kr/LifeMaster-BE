package com.example.LifeMaster_BE.Group.GroupChat;

import com.example.LifeMaster_BE.Group.GroupEntity;
import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import com.example.LifeMaster_BE.UserManager.Member.MemberRepository;
import com.example.LifeMaster_BE.Group.GroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

@RequiredArgsConstructor
@Controller
public class GroupChatController {

    private final SimpMessageSendingOperations messagingTemplate;
    private final GroupChatService chatService;
    private final GroupRepository groupRepository;
    private final MemberRepository memberRepository;

    @MessageMapping("/chat/send")
    public void sendMessage(GroupChatDto.ChatMessage message) {

        GroupEntity group = groupRepository.findById(message.getGroupId())
                .orElseThrow(() -> new RuntimeException("Group not found"));
        MemberEntity sender = memberRepository.findById(message.getSenderId())
                .orElseThrow(() -> new RuntimeException("Member not found"));

        // DB 저장
        chatService.saveMessage(message, group, sender);

        // 브로드캐스트
        messagingTemplate.convertAndSend(
                "/topic/chat/" + message.getGroupId(),
                message
        );
    }

    @MessageMapping("/chat/read")
    public void readMessage(GroupChatDto.ChatReadEvent event) {

        MemberEntity reader = memberRepository.findById(event.getReaderId())
                .orElseThrow(() -> new RuntimeException("Member not found"));

        // 읽음 처리
        chatService.markAsRead(event.getMessageId(), reader);

        // 브로드캐스트
        messagingTemplate.convertAndSend(
                "/topic/chat/read/" + event.getGroupId(),
                event
        );
    }
}
