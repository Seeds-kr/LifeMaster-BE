package com.example.LifeMaster_BE.Community.Comment.Like;

import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import com.example.LifeMaster_BE.UserManager.Member.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/comment/like")
@RequiredArgsConstructor
public class CommentLikeController {

    private final CommentLikeService likeService;
    private final MemberRepository memberRepository;

    @PostMapping("/{commentId}")
    public ResponseEntity<Map<String, Boolean>> like(@PathVariable Long commentId,
                                                     @AuthenticationPrincipal User user) {

        String email = user.getUsername();
        MemberEntity member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException(email));
        boolean isLiked = likeService.toggleLike(member.getId(), commentId);
        Map<String, Boolean> response = new HashMap<>();
        response.put("liked", isLiked);

        return ResponseEntity.ok(response);
    }


}
