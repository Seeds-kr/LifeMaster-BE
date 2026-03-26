package com.example.LifeMaster_BE.Group.GroupChat;

import com.example.LifeMaster_BE.Security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/groups")
public class GroupChatQueryController {

    private final GroupChatService chatService;

    // 채팅방 최초 로딩용
    @GetMapping("/{groupId}/chats")
    public List<GroupChatDto.ChatMessage> getChats(
            @PathVariable Long groupId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        return chatService.getChatsByGroup(user.getId(), groupId);
    }
}
