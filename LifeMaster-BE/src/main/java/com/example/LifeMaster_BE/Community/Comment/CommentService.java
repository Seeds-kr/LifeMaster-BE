package com.example.LifeMaster_BE.Community.Comment;

import com.example.LifeMaster_BE.Community.Comment.Dto.AllCommentsDto;
import com.example.LifeMaster_BE.Community.Comment.Like.LikeEntity;
import com.example.LifeMaster_BE.Community.Comment.Like.LikeRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;

    public List<AllCommentsDto> getAllComments(Long memberId){
        // 전체 댓글 가져오기
        List<CommentEntity> comments = commentRepository.findAll();

        // 댓글 id만 List로 추출
        List<Long> commentIds = comments.stream()
                .map(CommentEntity::getId)
                .toList();

        // Member-댓글 id 조합으로 유효한 like 엔티티가 있는지 조회
        List<LikeEntity> userLikes = likeRepository.findByMemberIdAndCommentIdIn(memberId, commentIds);

        // 조회 정보에서 comment Id만 Set
        Set<Long> likedCommentIds = userLikes.stream()
                .map(like -> like.getComment().getId())
                .collect(Collectors.toSet());

        return comments.stream()
                .map(comment -> new AllCommentsDto(
                        memberId,
                        comment.getComment(),
                        comment.getCommentDate(),
                        likedCommentIds.contains(comment.getId())
                ))
                .toList();
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
