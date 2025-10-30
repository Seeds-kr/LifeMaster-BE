package com.example.LifeMaster_BE.Community.Comment;

import com.example.LifeMaster_BE.Community.Comment.Dto.AllCommentsDto;
import com.example.LifeMaster_BE.Community.Comment.Like.CommentLikeEntity;
import com.example.LifeMaster_BE.Community.Comment.Like.CommentLikeRepository;
import com.example.LifeMaster_BE.Community.Post.PostEntity;
import com.example.LifeMaster_BE.Community.Post.PostRepository;
import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import com.example.LifeMaster_BE.UserManager.Member.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentLikeRepository likeRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private CommentService commentService;

    @Test
    @DisplayName("특정 게시글의 대한 전체 댓글과 사용자의 좋아요 여부 반환")
    void getAllComments_shouldReturnDtoList(){

        Long memberId = 1L;
        Long postId = 100L;

        PostEntity dummy = new PostEntity();
        CommentEntity comment1 = new CommentEntity("좋아요!", dummy);
        CommentEntity comment2 = new CommentEntity("괜찮아요!", dummy);

        comment1.setId(2L);
        comment2.setId(3L);

        List<CommentEntity> comments = List.of(comment1, comment2);

        CommentLikeEntity like = new CommentLikeEntity();
        like.setComment(comment1);

        when(commentRepository.findByPostId(postId)).thenReturn(comments);
        when(likeRepository.findByMemberIdAndCommentIdIn(eq(memberId), anyList()))
                .thenReturn(List.of(like));

        List<AllCommentsDto> result = commentService.getAllComments(memberId, postId);

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(AllCommentsDto::isLiked));
        assertTrue(result.stream().anyMatch(dto -> !dto.isLiked()));
    }

    @Test
    @DisplayName("댓글 작성 시 게시글과 회원 정보를 바탕으로 댓글을 저장한다")
    void createComment_shouldReturnSavedComment(){

        Long memberId = 1L;
        Long postId = 2L;
        String content = "댓글 내용";

        MemberEntity member = new MemberEntity();
        PostEntity post = new PostEntity();

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        CommentEntity savedComment = commentService.createComment(memberId, postId, content);
        assertEquals(content, savedComment.getComment());
        assertEquals(post, savedComment.getPost());
        assertEquals(0, post.getViewCount());
    }

    
    @Test
    @DisplayName("댓글 수정 시 해당 댓글의 내용을 갱신한다")
    void updateComment_shouldUpdateSuccessfully(){

        Long commentId = 5L;
        Long postId = 3L;
        String newContent = "수정된 댓글";

        PostEntity post = new PostEntity();
        CommentEntity comment = new CommentEntity("기존 댓글", post);
        comment.setId(commentId);

//        when(commentRepository.findByIdAndPostId(commentId, postId))
//                .thenReturn(Optional.of(comment));

//        commentService.updateComment(commentId, postId, newContent);

        assertEquals(newContent, comment.getComment());
        verify(commentRepository).save(comment);
    }

    @Test
    @DisplayName("댓글 삭제 시 해당 댓글을 삭제하고 게시글의 댓글 수를 감소시킨다")
    void deleteComment_shouldDeleteSuccessfully(){

        Long commentId = 5L;
        Long postId = 3L;

        PostEntity post = new PostEntity();
        post.increaseCommentCount();

        CommentEntity comment = new CommentEntity("삭제할 댓글", post);
        comment.setId(commentId);

//        when(commentRepository.findWithPostByIdAndPostId(commentId, postId))
//                .thenReturn(Optional.of(comment));

//        commentService.deleteComment(commentId, postId);

        assertEquals(0, post.getCommentCount());
        verify(commentRepository).deleteByIdAndPostId(commentId, postId);
    }
}