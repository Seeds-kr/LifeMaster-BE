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

    public boolean toggleLike(Long memberID, Long commentId){

        Optional<CommentLikeEntity> existingLike = likeRepository.findByMemberIdAndCommentId(memberID, commentId);

        if(existingLike.isPresent()){
            likeRepository.deleteByMemberIdAndCommentId(memberID, commentId);
            return false;
        } else {
            CommentEntity comment = commentRepository.findById(commentId)
                    .orElseThrow(() -> new EntityNotFoundException("Comment Not found"));

            CommentLikeEntity newLike = new CommentLikeEntity(memberID, comment);

            likeRepository.save(newLike);
            return true;
        }
    }
}
