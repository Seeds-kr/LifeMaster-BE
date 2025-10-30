package com.example.LifeMaster_BE.Community.Post;

import com.example.LifeMaster_BE.Community.Post.Dto.AllPostsDto;
import com.example.LifeMaster_BE.Community.Post.Dto.PostGetResponse;
import com.example.LifeMaster_BE.Community.Post.Like.PostLikeEntity;
import com.example.LifeMaster_BE.Community.Post.Like.PostLikeRepository;
import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import com.example.LifeMaster_BE.UserManager.Member.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PostService {

    private final PostRepository postRepository;
    private final PostLikeRepository likeRepository;
    private final MemberRepository memberRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String POPULAR_POSTS_KEY = "popularPosts"; // 인기글 캐싱 키

    public List<AllPostsDto> getAllPosts(Long memberId, PostType type) {
        List<PostEntity> posts = postRepository.findByType(type, Sort.by(Sort.Direction.DESC, "createdAt"));

        List<Long> postIds = posts.stream()
                .map(PostEntity::getId)
                .toList();

        List<PostLikeEntity> userLikes = likeRepository.findByMemberIdAndPostIdIn(memberId, postIds);

        Set<Long> likedPostIds = userLikes.stream()
                .map(like -> like.getPost().getId())
                .collect(Collectors.toSet());

        return posts.stream()
                .map(post -> new AllPostsDto(
                        post.getId(),
                        post.getTitle(),
                        post.getMember().getNickname(),
                        post.getViewCount(),
                        post.getCommentCount(),
                        post.getLikes().size(),
                        post.getCreatedAt(),
                        likedPostIds.contains(post.getId())
                ))
                .toList();
    }

    public PostGetResponse getPost(Long postId, Long memberId){
        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found"));
        boolean liked = likeRepository.existsByMemberIdAndPostId(memberId, postId);
        PostGetResponse response = new PostGetResponse(
                post.getTitle(),
                post.getContent(),
                post.getFile(),
                post.getType(),
                post.getMember().getId(),
                post.getMember().getNickname(),
                liked,
                post.getViewCount(),
                post.getLikes().size(),
                memberId.equals(post.getMember().getId()),
                post.getCreatedAt()
        );

        // 게시글 조회 시 조회수 증가 (DB 반영)
        postRepository.increaseViewCount(postId);
        postRepository.save(post); // 변경 감지를 위한 저장
        return response;
    }

    public PostEntity createPost(String title, String content, String fileUrl,
                                 PostType type, Long memberId) {

        MemberEntity member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("Member not found"));

        PostEntity postEntity = new PostEntity(title, content, fileUrl, type);
        PostEntity postEntity1 = postEntity.toBuilder()
                .member(member)
                .build();

        return postRepository.save(postEntity1);
    }

    public void updatePost(Long postId, String title, String content, String fileUrl, Long memberId){
        PostEntity postEntity = postRepository.findByIdAndMemberId(postId, memberId)
                .orElseThrow(() -> new RuntimeException("수정 권한이 없습니다."));

        postEntity.updatePost(title, content, fileUrl);
        postRepository.save(postEntity);
    }

    public void deletePost(Long postId){
        postRepository.deleteById(postId);
    }

    // 인기글 갱신 (Redis에 저장)
    public void updatePopularPosts() {
        try {
            List<Long> popularPostIds = postRepository.findTop2ByOrderByViewCountDesc().stream()
                    .map(PostEntity::getId)
                    .collect(Collectors.toList());

            redisTemplate.opsForValue().set(POPULAR_POSTS_KEY, popularPostIds, 10, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.error("🔥 Redis 저장 중 오류 발생: ", e);
        }
    }


    // 10분마다 인기글 갱신
    @Scheduled(fixedRate = 600000) // 10분마다 실행
    public void refreshPopularPosts() {
        updatePopularPosts();
    }

    // 캐싱된 인기글 가져오기
    public List<AllPostsDto> getPopularPosts(Long memberId) {
        List<Long> cachedPostIds = (List<Long>) redisTemplate.opsForValue().get(POPULAR_POSTS_KEY);

        if (cachedPostIds == null || cachedPostIds.isEmpty()) { // Redis에 없으면 갱신
            updatePopularPosts();
            cachedPostIds = (List<Long>) redisTemplate.opsForValue().get(POPULAR_POSTS_KEY);
        }
        log.info(cachedPostIds.toString());
        List<PostEntity> popularPosts = postRepository.findByIdIn(cachedPostIds);
        List<PostLikeEntity> userLikes = likeRepository.findByMemberIdAndPostIdIn(memberId, cachedPostIds);
        Set<Long> likedPostIds = userLikes.stream()
                .map(like -> like.getPost().getId())
                .collect(Collectors.toSet());

        return popularPosts.stream()
                .map(post -> new AllPostsDto(
                        post.getId(),
                        post.getTitle(),
                        post.getMember().getNickname(),
                        post.getViewCount(),
                        post.getCommentCount(),
                        post.getLikes().size(),
                        post.getCreatedAt(),
                        likedPostIds.contains(post.getId())
                ))
                .toList();
    }
}