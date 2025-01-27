package com.example.LifeMaster_BE.Community.Post.Like;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLikeEntity, Long> {

    Optional<PostLikeEntity> findByMemberIdAndPostId(Long memberId, Long PostId);
    void deleteByMemberIdAndPostId(Long memberId, Long PostId);
    List<PostLikeEntity> findByMemberIdAndPostIdIn(Long memberId, List<Long> PostIds);
}
