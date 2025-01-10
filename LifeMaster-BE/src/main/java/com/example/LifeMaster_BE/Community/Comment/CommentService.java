package com.example.LifeMaster_BE.Community.Comment;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;

    public List<CommentEntity> getAllComments(){
        return commentRepository.findAll();
    }
    public CommentEntity createComment(String comment) {
        CommentEntity commentEntity = new CommentEntity(comment);
        return commentRepository.save(commentEntity);
    }

    public void updateComment(Long commentId, String comment){
        CommentEntity commentEntity = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("comment not found"));

        commentEntity.updateComment(comment);
        commentRepository.save(commentEntity);
    }

    public void deleteComment(Long commentId){
        commentRepository.deleteById(commentId);
    }
}
