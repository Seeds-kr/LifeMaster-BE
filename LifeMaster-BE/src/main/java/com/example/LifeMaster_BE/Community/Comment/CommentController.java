package com.example.LifeMaster_BE.Community.Comment;

import com.example.LifeMaster_BE.Community.Comment.Dto.AllCommentsDto;
import com.example.LifeMaster_BE.Community.Comment.Dto.CommentDto;
import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import com.example.LifeMaster_BE.UserManager.Member.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/comment")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final MemberRepository memberRepository;

    @GetMapping
    public ResponseEntity<List<AllCommentsDto>> getAllComments(@AuthenticationPrincipal User user) {

        String email = user.getUsername();
        MemberEntity member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException(email));

        List<AllCommentsDto> allComments = commentService.getAllComments(member.getId());
        return ResponseEntity.ok(allComments);
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
