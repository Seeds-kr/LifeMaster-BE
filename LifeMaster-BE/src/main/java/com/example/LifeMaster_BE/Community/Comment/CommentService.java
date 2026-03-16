package com.example.LifeMaster_BE.Community.Comment;

import com.example.LifeMaster_BE.Community.Comment.Dto.AllCommentsDto;
import com.example.LifeMaster_BE.Community.Comment.Like.CommentLikeEntity;
import com.example.LifeMaster_BE.Community.Comment.Like.CommentLikeRepository;
import com.example.LifeMaster_BE.Community.Post.PostEntity;
import com.example.LifeMaster_BE.Community.Post.PostRepository;
import com.example.LifeMaster_BE.Exception.CustomException.ForbiddenActionException;
import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import com.example.LifeMaster_BE.UserManager.Member.MemberRepository;
import jakarta.persistence.EntityNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final CommentLikeRepository likeRepository;
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;

    public List<AllCommentsDto> getAllComments(Long memberId, Long postId){
        // 전체 댓글 가져오기
        List<CommentEntity> comments = commentRepository.findByPostId(postId);

        // 댓글 id만 List로 추출
        List<Long> commentIds = comments.stream()
                .map(CommentEntity::getId)
                .toList();

        // Member-댓글 id 조합으로 유효한 like 엔티티가 있는지 조회
        List<CommentLikeEntity> userLikes = likeRepository.findByMemberIdAndCommentIdIn(memberId, commentIds);

        // 조회 정보에서 comment Id만 Set
        Set<Long> likedCommentIds = userLikes.stream()
                .map(like -> like.getComment().getId())
                .collect(Collectors.toSet());

        return comments.stream()
                .map(comment -> new AllCommentsDto(
                        comment.getId(),
                        comment.getMember().getId(),
                        comment.getComment(),
                        comment.getMember().getNickname(),
                        comment.getLikes().size(),
                        likedCommentIds.contains(comment.getId()),
                        memberId.equals(comment.getMember().getId()),
                        comment.getCreatedAt()
                ))
                .toList();
    }

    public CommentEntity createComment(Long memberId, Long postId, String comment) {

        MemberEntity member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("member not found"));

        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("post not found"));

        CommentEntity commentEntity = CommentEntity.builder()
                .comment(comment)
                .member(member)
                .post(post)
                .build();

        commentRepository.save(commentEntity);      // 제거 가능 - 변경감지
        post.increaseCommentCount();

        return commentEntity;
    }

    public void updateComment(Long commentId, Long postId, String comment, Long memberId){
        CommentEntity commentEntity = commentRepository.findByIdAndPostIdAndMemberId(commentId, postId, memberId)
                .orElseThrow(() -> new ForbiddenActionException("본인 댓글만 수정할 수 있습니다."));

        commentEntity.updateComment(comment);
        commentRepository.save(commentEntity);
    }

    public void deleteComment(Long commentId, Long postId, Long memberId){
        CommentEntity comment = commentRepository.findWithPostByIdAndPostIdAndMemberId(commentId, postId, memberId)
                .orElseThrow(() -> new ForbiddenActionException("본인 댓글만 삭제할 수 있습니다."));
        PostEntity post = comment.getPost();
        post.decreaseCommentCount();

        commentRepository.deleteByIdAndPostId(commentId, postId);
    }
}
