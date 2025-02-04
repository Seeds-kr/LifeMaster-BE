package com.example.LifeMaster_BE.Community.Post;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<PostEntity, Long> {
    List<PostEntity> findByType(PostType type);
    Optional<PostEntity> findByIdAndMemberId(Long postId, Long memberId);
}
