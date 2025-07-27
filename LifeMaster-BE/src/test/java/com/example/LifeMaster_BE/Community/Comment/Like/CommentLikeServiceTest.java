package com.example.LifeMaster_BE.Community.Comment.Like;

import com.example.LifeMaster_BE.Community.Comment.CommentEntity;
import com.example.LifeMaster_BE.Community.Comment.CommentRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentLikeServiceTest {

    @Mock
    private CommentLikeRepository likeRepository;

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private CommentLikeService commentLikeService;

    private final Long memberId = 1L;
    private final Long commentId = 100L;

    @Test
    @DisplayName("좋아요가 있다면 삭제되고 false 반환")
    void toggleLike_existingLike(){

        CommentLikeEntity existingLike = new CommentLikeEntity(memberId);
        when(likeRepository.findByMemberIdAndCommentId(memberId, commentId))
                .thenReturn(Optional.of(existingLike));

        boolean result = commentLikeService.toggleLike(memberId, commentId);

        verify(likeRepository).deleteByMemberIdAndCommentId(memberId, commentId);
        assertFalse(result);
    }

    @Test
    @DisplayName("좋아요가 존재하지 않으면 추가되고 true 반환")
    void toggleLike_newLike(){

        when(likeRepository.findByMemberIdAndCommentId(memberId, commentId))
                .thenReturn(Optional.empty());

        CommentEntity comment = mock(CommentEntity.class);
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        boolean result = commentLikeService.toggleLike(memberId, commentId);
        verify(commentRepository).findById(commentId);
        verify(comment).addLike(any(CommentLikeEntity.class));
        verify(likeRepository).save(any(CommentLikeEntity.class));
        assertTrue(result);
    }

    @Test
    @DisplayName("댓글 존재하지 않으면 예외 발생")
    void toggleLike_commentNotFound(){
        when(likeRepository.findByMemberIdAndCommentId(memberId, commentId))
                .thenReturn(Optional.empty());
        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> commentLikeService.toggleLike(memberId, commentId));
    }

}