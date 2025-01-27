package com.example.LifeMaster_BE.Community.Post.Like;

import com.example.LifeMaster_BE.Community.Post.PostEntity;
import com.example.LifeMaster_BE.Community.Post.PostRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class PostLikeService {

    private final PostLikeRepository likeRepository;
    private final PostRepository postRepository;

    public boolean toggleLike(Long memberId, Long postId) {

        Optional<PostLikeEntity> existingLike = likeRepository.findByMemberIdAndPostId(memberId, postId);

        if (existingLike.isPresent()) {
            likeRepository.deleteByMemberIdAndPostId(memberId, postId);
            return false;
        } else {
            PostEntity post = postRepository.findById(postId)
                    .orElseThrow(() -> new EntityNotFoundException("Post Not found"));

            PostLikeEntity newLike = new PostLikeEntity(memberId, post);

            likeRepository.save(newLike);
            return true;
        }
    }
}
