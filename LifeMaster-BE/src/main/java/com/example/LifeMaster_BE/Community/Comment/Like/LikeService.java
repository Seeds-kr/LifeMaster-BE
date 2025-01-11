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
public class LikeService {

    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;
    public boolean toggleLike(Long userId, Long commentId){

        Optional<LikeEntity> existingLike = likeRepository.findByUserIdAndCommentId(userId, commentId);


        if(existingLike.isPresent()){
            likeRepository.deleteByUserIdAndCommentId(userId, commentId);
            return false;
        } else {
            CommentEntity comment = commentRepository.findById(commentId)
                    .orElseThrow(() -> new EntityNotFoundException("Comment Not found"));

            LikeEntity newLike = new LikeEntity(userId, comment);

            likeRepository.save(newLike);
            return true;
        }
    }
}
