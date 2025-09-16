package com.example.LifeMaster_BE.Community.Comment;

import com.example.LifeMaster_BE.Community.Comment.Dto.AllCommentsDto;
import com.example.LifeMaster_BE.Community.Comment.Dto.CommentCreateRequest;
import com.example.LifeMaster_BE.Security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/posts/{postId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @GetMapping
    public ResponseEntity<List<AllCommentsDto>> getAllComments(
            @Parameter(description = "게시글 ID", required = true)
            @PathVariable(name = "postId") Long postId,
            @AuthenticationPrincipal CustomUserDetails user) {

        List<AllCommentsDto> allComments = commentService.getAllComments(user.getId(), postId);
        return ResponseEntity.ok(allComments);
    }

    @PostMapping
    public ResponseEntity<Map<String, Long>> newComment(
            @Parameter(description = "게시글 ID", required = true)
            @PathVariable("postId") Long postId,
            @RequestBody CommentCreateRequest commentDto,
            @AuthenticationPrincipal CustomUserDetails user) {

        String comment = commentDto.getComment();
        CommentEntity createdComment = commentService.createComment(user.getId(), postId, comment);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("commentId", createdComment.getId()));
    }

    @PatchMapping("/{commentId}")
    public void updateComment(
            @Parameter(description = "게시글 ID", required = true)
            @PathVariable("postId") Long postId,
            @Parameter(description = "댓글 ID", required = true)
            @PathVariable("commentId") Long commentId,
            @RequestBody CommentCreateRequest commentDto){
        commentService.updateComment(commentId, postId, commentDto.getComment());
    }

    @DeleteMapping("/{commentId}")
    public void deleteComment(
            @Parameter(description = "게시글 ID", required = true)
            @PathVariable("postId") Long postId,
            @Parameter(description = "댓글 ID", required = true)
            @PathVariable("commentId") Long commentId){
        commentService.deleteComment(commentId, postId);
    }
}
