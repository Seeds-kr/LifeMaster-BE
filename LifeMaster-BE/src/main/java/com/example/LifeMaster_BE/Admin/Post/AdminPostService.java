package com.example.LifeMaster_BE.Admin.Post;

import com.example.LifeMaster_BE.Admin.Dto.AdminPostResponseDto;
import com.example.LifeMaster_BE.Admin.Dto.AdminPostSummaryDto;
import com.example.LifeMaster_BE.Community.Post.PostEntity;
import com.example.LifeMaster_BE.Community.Post.PostRepository;
import com.example.LifeMaster_BE.Community.Post.PostType;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminPostService {

    private final PostRepository postRepository;

    /**
     * 관리자용 게시글 목록 조회.
     */
    @Transactional(readOnly = true)
    public Page<AdminPostResponseDto> getPosts(
            String keyword,
            PostType type,
            int page,
            int size
    ) {
        String normalizedKeyword = normalizeKeyword(keyword);

        Long numericKeyword = parseLongOrNull(normalizedKeyword);

        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                normalizePageSize(size),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        return postRepository.searchAdminPosts(
                normalizedKeyword,
                type,
                numericKeyword,
                numericKeyword,
                pageable
        );
    }

    /**
     * 게시글 통계 요약 조회.
     */
    @Transactional(readOnly = true)
    public AdminPostSummaryDto getPostSummary() {
        long totalCount = postRepository.count();
        long freeCount = postRepository.countByType(PostType.FREE);
        long improvementCount =
                postRepository.countByType(PostType.IMPROVEMENT);
        long calendarSharedCount =
                postRepository.countByCalendarSharedTrue();

        return new AdminPostSummaryDto(
                totalCount,
                freeCount,
                improvementCount,
                calendarSharedCount
        );
    }

    /**
     * 관리자 권한으로 게시글 삭제.
     *
     * 일반 게시글 삭제와 달리 작성자 본인 확인을 하지 않습니다.
     */
    @Transactional
    public void deletePost(Long postId) {
        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "삭제할 게시글을 찾을 수 없습니다. postId=" + postId
                        )
                );

        postRepository.delete(post);
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null) {
            return null;
        }

        String trimmed = keyword.trim();

        return trimmed.isEmpty() ? null : trimmed;
    }

    private Long parseLongOrNull(String value) {
        if (value == null) {
            return null;
        }

        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private int normalizePageSize(int size) {
        if (size < 1) {
            return 10;
        }

        return Math.min(size, 100);
    }
}
