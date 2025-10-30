package com.example.LifeMaster_BE.Community.Post;

import com.example.LifeMaster_BE.Community.Post.Dto.AllPostsDto;
import com.example.LifeMaster_BE.Community.Post.Like.PostLikeEntity;
import com.example.LifeMaster_BE.Community.Post.Like.PostLikeRepository;
import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import com.example.LifeMaster_BE.UserManager.Member.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private PostLikeRepository likeRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private PostService postService;

    private MemberEntity member;
    private PostEntity post;

    @BeforeEach
    void setUp() {
        member = new MemberEntity();
        post = new PostEntity("title", "content", "fileurl", PostType.FREE);
        post.setId(1L);
        PostEntity post1 = post.toBuilder()
                .member(member)
                .build();
    }

    @Test
    @DisplayName("모든 게시글 조회 - 좋아요 여부 포함")
    void getAllPosts_shouldReturnPostsWithLikeStatus(){
        Long memberId = 1L;

        PostEntity post1 = new PostEntity("title1", "content1", "fileurl1", PostType.FREE);
        PostEntity post2 = new PostEntity("title2", "content2", "fileurl2", PostType.FREE);
        post1.setId(1L);
        post2.setId(2L);

        PostEntity post11 = post1.toBuilder()
                .member(member)
                .build();
        PostEntity post22 = post2.toBuilder()
                .member(member)
                .build();

        List<PostEntity> posts = List.of(post11, post22);
        PostLikeEntity like = new PostLikeEntity();
        like.setPost(post11);

        when(postRepository.findByType(eq(PostType.FREE), any(Sort.class))).thenReturn(posts);
        when(likeRepository.findByMemberIdAndPostIdIn(eq(memberId), anyList()))
                .thenReturn(List.of(like));

        List<AllPostsDto> result = postService.getAllPosts(memberId, PostType.FREE);

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(AllPostsDto::isLiked));
        assertTrue(result.stream().anyMatch(dto -> !dto.isLiked()));
    }

    @Test
    @DisplayName("특정 게시글 조회")
    void getPost_shouldReturnPostAndIncreaseViewCount(){
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

//        PostEntity result = postService.getPost(1L);

        verify(postRepository).increaseViewCount(1L);
        verify(postRepository).save(post);
//        assertEquals(post, result);
    }

    @Test
    @DisplayName("게시글 생성")
    void createPost_shouldSavePost(){
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(postRepository.save(any(PostEntity.class))).thenReturn(post);

        PostEntity result = postService.createPost("title", "content",
                "fileurl", PostType.FREE, 1L);

        assertEquals(post, result);
    }

    @Test
    @DisplayName("게시글 업데이트")
    void updatePost_shouldUpdateSuccessfully(){
        when(postRepository.findByIdAndMemberId(1L, 1L))
                .thenReturn(Optional.of(post));

        postService.updatePost(1L, "newTitle", "new content", "new url", 1L);
        assertEquals("newTitle", post.getTitle());
    }

    @Test
    @DisplayName("게시글 삭제")
    void deletePost_shouldCallDelete(){
//        postService.deletePost(1L);
        verify(postRepository).deleteById(1L);
    }

    @Test
    @DisplayName("인기글 업데이트 - Redis")
    void updatePopularPosts_shouldCachePostIdsInRedis(){
        when(postRepository.findTop2ByOrderByViewCountDesc()).thenReturn(List.of(post));
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        postService.updatePopularPosts();

        verify(valueOperations).set(eq("popularPosts"),
                eq(List.of(1L)), eq(10L), eq(TimeUnit.MINUTES));
    }

//    @Test
//    @DisplayName("인기글 가져오기 - Redis")
//    void getPopularPosts_shouldReturnCachedData(){
//        List<PostEntity> cached = List.of(post);
//        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
//        when(valueOperations.get("popularPosts")).thenReturn(cached);
//
//        List<AllPostsDto> result = postService.getPopularPosts(1L);
//
//        assertEquals(1, result.size());
//        assertEquals(cached, result);
//    }
}