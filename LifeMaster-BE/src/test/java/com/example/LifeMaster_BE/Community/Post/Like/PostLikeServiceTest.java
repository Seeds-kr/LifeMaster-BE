package com.example.LifeMaster_BE.Community.Post.Like;

import com.example.LifeMaster_BE.Community.Post.PostEntity;
import com.example.LifeMaster_BE.Community.Post.PostRepository;
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
class PostLikeServiceTest {

    @Mock
    private PostLikeRepository likeRepository;

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private PostLikeService postLikeService;

    private final Long memberId = 1L;
    private final Long postId = 100L;

    @Test
    @DisplayName("좋아요가 있다면 삭제되고 false 반환")
    void toggleLike_existingLike(){

        PostLikeEntity existingLike = new PostLikeEntity(memberId);
        when(likeRepository.findByMemberIdAndPostId(memberId, postId))
                .thenReturn(Optional.of(existingLike));

        boolean result = postLikeService.toggleLike(memberId, postId);

        verify(likeRepository).deleteByMemberIdAndPostId(memberId, postId);
        assertFalse(result);
    }

    @Test
    @DisplayName("좋아요가 존재하지 않으면 추가되고 true 반환")
    void toggleLike_newLike(){

        when(likeRepository.findByMemberIdAndPostId(memberId, postId))
                .thenReturn(Optional.empty());

        PostEntity post = mock(PostEntity.class);
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        boolean result = postLikeService.toggleLike(memberId, postId);

        verify(postRepository).findById(postId);
        verify(post).addLike(any(PostLikeEntity.class));
        verify(likeRepository).save(any(PostLikeEntity.class));
        assertTrue(result);
    }

    @Test
    @DisplayName("게시글이 존재하지 않으면 예외 발생")
    void toggleLike_postNotFound(){
        when(likeRepository.findByMemberIdAndPostId(memberId, postId))
                .thenReturn(Optional.empty());
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> postLikeService.toggleLike(memberId, postId));
    }
}