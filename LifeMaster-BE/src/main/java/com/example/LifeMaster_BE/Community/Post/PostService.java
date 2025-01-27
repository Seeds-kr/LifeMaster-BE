package com.example.LifeMaster_BE.Community.Post;

import com.example.LifeMaster_BE.Community.Comment.CommentEntity;
import com.example.LifeMaster_BE.Community.Post.Dto.AllPostsDto;
import com.example.LifeMaster_BE.Community.Post.Like.PostLikeEntity;
import com.example.LifeMaster_BE.Community.Post.Like.PostLikeRepository;
import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final PostLikeRepository likeRepository;

    public List<AllPostsDto> getAllPosts(Long memberId, PostType type) {
        List<PostEntity> posts = postRepository.findByType(type);

        List<Long> postIds = posts.stream()
                .map(PostEntity::getId)
                .toList();

        List<PostLikeEntity> userLikes = likeRepository.findByMemberIdAndPostIdIn(memberId, postIds);

        Set<Long> likedPostIds = userLikes.stream()
                .map(like -> like.getPost().getId())
                .collect(Collectors.toSet());

        return posts.stream()
                .map(post -> new AllPostsDto(
                        post.getTitle(),
                        post.getMember().getNickname(),
                        post.getViewCount(),
                        post.getCreatedAt(),
                        likedPostIds.contains(post.getId())
                ))
                .toList();
    }

    public PostEntity getPost(Long postId){
        return postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found"));
    }

    public PostEntity createPost(String title, String content, String fileUrl,
                                 PostType type, MemberEntity member) {
        PostEntity postEntity = new PostEntity(title, content, fileUrl, type, member);
        return postRepository.save(postEntity);
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
}
