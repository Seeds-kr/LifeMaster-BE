package com.example.LifeMaster_BE.Community.Post.Like;

import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import com.example.LifeMaster_BE.UserManager.Member.MemberRepository;
import io.swagger.v3.oas.annotations.Parameter;
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
@RequestMapping("/posts/like")
@RequiredArgsConstructor
public class PostLikeController {

    private final PostLikeService likeService;
    private final MemberRepository memberRepository;

    @PostMapping("/{postId}")
    public ResponseEntity<Map<String, Boolean>> like (
            @Parameter(description = "게시글 ID", required = true)
            @PathVariable("postId") Long postId,
            @AuthenticationPrincipal User user){

        String email = user.getUsername();
        MemberEntity member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException(email));

        boolean isLiked = likeService.toggleLike(member.getId(), postId);
        Map<String, Boolean> response = new HashMap<>();
        response.put("liked", isLiked);

        return ResponseEntity.ok(response);
    }
}
