package com.example.LifeMaster_BE.Admin.Content;

import com.example.LifeMaster_BE.Admin.Content.Dto.AdminCommentListDto;
import com.example.LifeMaster_BE.Admin.Content.Dto.AdminPostDetailDto;
import com.example.LifeMaster_BE.Admin.Content.Dto.AdminPostListDto;
import com.example.LifeMaster_BE.Community.Comment.CommentEntity;
import com.example.LifeMaster_BE.Community.Comment.CommentRepository;
import com.example.LifeMaster_BE.Community.Post.PostEntity;
import com.example.LifeMaster_BE.Community.Post.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminContentService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    public Page<AdminPostListDto> getPosts(String keyword, Pageable pageable) {
        Page<PostEntity> posts;
        if (keyword != null && !keyword.isBlank()) {
            posts = postRepository.searchPosts(keyword, pageable);
        } else {
            posts = postRepository.findAll(pageable);
        }
        return posts.map(AdminPostListDto::from);
    }

    public AdminPostDetailDto getPostDetail(Long postId) {
        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다. ID: " + postId));
        return AdminPostDetailDto.from(post);
    }

    @Transactional
    public void deletePost(Long postId) {
        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다. ID: " + postId));
        postRepository.delete(post);
    }

    public Page<AdminCommentListDto> getComments(String keyword, Pageable pageable) {
        Page<CommentEntity> comments;
        if (keyword != null && !keyword.isBlank()) {
            comments = commentRepository.searchComments(keyword, pageable);
        } else {
            comments = commentRepository.findAll(pageable);
        }
        return comments.map(AdminCommentListDto::from);
    }

    @Transactional
    public void deleteComment(Long commentId) {
        CommentEntity comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다. ID: " + commentId));

        // 게시글의 댓글 수 감소
        if (comment.getPost() != null) {
            comment.getPost().decreaseCommentCount();
        }

        commentRepository.delete(comment);
    }
}
