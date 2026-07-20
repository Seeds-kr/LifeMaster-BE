package com.example.LifeMaster_BE.Admin.Post;

import com.example.LifeMaster_BE.Admin.Dto.AdminPostResponseDto;
import com.example.LifeMaster_BE.Admin.Dto.AdminPostSummaryDto;
import com.example.LifeMaster_BE.Community.Post.PostType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(
        name = "Admin - Post",
        description = "관리자 게시글 관리 API"
)
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
            @Parameter(description = "검색 키워드")
            @RequestParam(required = false) String keyword,

            @Parameter(description = "게시글 타입")
            @RequestParam(required = false) PostType type,

            @Parameter(description = "페이지 번호")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "페이지 크기")
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<AdminPostResponseDto> response =
                adminPostService.getPosts(
                        keyword,
                        type,
                        page,
                        size
                );

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "관리자 게시글 통계 조회",
            description = "전체 게시글과 게시글 유형별 개수를 조회합니다."
    )
    @GetMapping("/summary")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminPostSummaryDto> getPostSummary() {
        AdminPostSummaryDto response =
                adminPostService.getPostSummary();

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "관리자 게시글 상세 조회",
            description = "관리자가 특정 게시글의 상세 정보를 조회합니다."
    )
    @GetMapping("/{postId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminPostResponseDto> getPostDetail(
            @Parameter(description = "게시글 ID")
            @PathVariable Long postId
    ) {
        AdminPostResponseDto response =
                adminPostService.getPostDetail(postId);

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "관리자 게시글 삭제",
            description = "관리자가 특정 게시글을 삭제합니다."
    )
    @DeleteMapping("/{postId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePost(
            @Parameter(description = "게시글 ID")
            @PathVariable Long postId
    ) {
        adminPostService.deletePost(postId);

        return ResponseEntity.noContent().build();
    }
}