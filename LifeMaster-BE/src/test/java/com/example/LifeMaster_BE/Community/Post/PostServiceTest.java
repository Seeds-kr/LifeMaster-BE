package com.example.LifeMaster_BE.Community.Post;

import com.example.LifeMaster_BE.Community.Post.Like.PostLikeRepository;
import com.example.LifeMaster_BE.UserManager.Member.MemberRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;

import static org.junit.jupiter.api.Assertions.*;

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

    @InjectMocks
    private PostService postService;

}