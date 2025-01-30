package com.example.LifeMaster_BE.Community.Comment;

import com.example.LifeMaster_BE.Community.Comment.Dto.AllCommentsDto;
import com.example.LifeMaster_BE.Community.Comment.Dto.CommentDto;
import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import com.example.LifeMaster_BE.UserManager.Member.MemberRepository;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/posts/{postId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final MemberRepository memberRepository;

    @GetMapping
    public ResponseEntity<List<AllCommentsDto>> getAllComments(
            @Parameter(description = "게시글 ID", required = true)
            @PathVariable(name = "postId") Long postId,
            @AuthenticationPrincipal User user) {

        String email = user.getUsername();
        MemberEntity member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException(email));

        List<AllCommentsDto> allComments = commentService.getAllComments(member.getId(), postId);
        return ResponseEntity.ok(allComments);
    }

    @PostMapping
    public ResponseEntity<CommentEntity> newComment(
            @Parameter(description = "게시글 ID", required = true)
            @PathVariable("postId") Long postId,
            @RequestBody CommentDto commentDto){
        String comment = commentDto.getComment();
        CommentEntity createdComment = commentService.createComment(postId, comment);

        return ResponseEntity.status(HttpStatus.CREATED).body(createdComment);
    }

    @PatchMapping("/{commentId}")
    public void updateComment(
            @Parameter(description = "게시글 ID", required = true)
            @PathVariable("postId") Long postId,
            @Parameter(description = "댓글 ID", required = true)
            @PathVariable("commentId") Long commentId,
            @RequestBody CommentDto commentDto){
        commentService.updateComment(commentId, postId, commentDto.getComment());
    }

    @DeleteMapping("/{commentId}")
    public void deleteComment(
            @Parameter(description = "게시글 ID", required = true)
            @PathVariable("postId") Long postId,
            @Parameter(description = "댓글 ID", required = true)
            @PathVariable("commentId") Long commentId){
        commentService.deleteComment(postId, commentId);
    }
}
