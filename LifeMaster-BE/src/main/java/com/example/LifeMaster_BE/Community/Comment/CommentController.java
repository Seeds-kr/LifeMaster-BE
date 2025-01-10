package com.example.LifeMaster_BE.Community.Comment;

import com.example.LifeMaster_BE.Community.Comment.Dto.AllCommentsDto;
import com.example.LifeMaster_BE.Community.Comment.Dto.CommentDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/comment")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @GetMapping
    public ResponseEntity<List<AllCommentsDto>> getAllComments() {
        List<CommentEntity> comments = commentService.getAllComments();

        List<AllCommentsDto> allCommentsDTOs = comments.stream()
                .map(comment -> new AllCommentsDto(
                        "RandomMember",
                        comment.getComment(),
                        comment.getCommentDate()
                )).toList();

        return ResponseEntity.ok(allCommentsDTOs);
    }
    @PostMapping
    public ResponseEntity<CommentEntity> newComment(@RequestBody CommentDto commentDto){
        String comment = commentDto.getComment();
        CommentEntity createdComment = commentService.createComment(comment);

        return ResponseEntity.status(HttpStatus.CREATED).body(createdComment);
    }

    @PatchMapping("/{commentId}")
    public void updateComment(@PathVariable Long commentId,
                              @RequestBody CommentDto commentDto){
        commentService.updateComment(commentId, commentDto.getComment());
    }

    @DeleteMapping("/{commentId}")
    public void deleteComment(@PathVariable Long commentId){
        commentService.deleteComment(commentId);
    }
}
