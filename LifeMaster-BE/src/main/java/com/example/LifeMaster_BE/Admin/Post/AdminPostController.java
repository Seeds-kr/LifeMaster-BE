package com.example.LifeMaster_BE.Admin.Post;

import com.example.LifeMaster_BE.Admin.Dto.AdminPostResponseDto;
import com.example.LifeMaster_BE.Admin.Dto.AdminPostSummaryDto;
import com.example.LifeMaster_BE.Community.Post.PostType;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/posts")
@RequiredArgsConstructor
public class AdminPostController {

    private final AdminPostService adminPostService;

    @Operation(
            summary = "관리자 게시글 목록 조회",
            description = """
                    게시글을 페이징으로 조회합니다.
                    게시글 ID, 회원 ID, 제목, 내용, 이메일, 닉네임 검색을 지원합니다.
                    게시글 타입 필터를 적용할 수 있습니다.
                    """
    )
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<AdminPostResponseDto>> getPosts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) PostType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<AdminPostResponseDto> response =
                adminPostService.getPosts(keyword, type, page, size);

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "관리자 게시글 통계 조회",
            description = "전체 게시글과 게시글 유형별 개수를 조회합니다."
    )
    @GetMapping("/summary")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminPostSummaryDto> getPostSummary() {
        return ResponseEntity.ok(
                adminPostService.getPostSummary()
        );
    }

    @Operation(
            summary = "관리자 게시글 삭제",
            description = "관리자가 특정 게시글을 삭제합니다."
    )
    @DeleteMapping("/{postId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePost(
            @PathVariable Long postId
    ) {
        adminPostService.deletePost(postId);

        return ResponseEntity.noContent().build();
    }
}