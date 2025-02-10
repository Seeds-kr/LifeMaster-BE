package com.example.LifeMaster_BE.Community.Comment.Like;

import com.example.LifeMaster_BE.Community.Comment.CommentEntity;
import com.example.LifeMaster_BE.Community.Comment.CommentRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class CommentLikeService {

    private final CommentLikeRepository likeRepository;
    private final CommentRepository commentRepository;

    public boolean toggleLike(Long memberId, Long commentId){

        Optional<CommentLikeEntity> existingLike = likeRepository.findByMemberIdAndCommentId(memberId, commentId);

        if(existingLike.isPresent()){
            likeRepository.deleteByMemberIdAndCommentId(memberId, commentId);
            return false;
        } else {
            CommentEntity comment = commentRepository.findById(commentId)
                    .orElseThrow(() -> new EntityNotFoundException("Comment Not found"));

            CommentLikeEntity newLike = new CommentLikeEntity(memberId);
            comment.addLike(newLike);
            likeRepository.save(newLike);
            return true;
        }
    }
}
